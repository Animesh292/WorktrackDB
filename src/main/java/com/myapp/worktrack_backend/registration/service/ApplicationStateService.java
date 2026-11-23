package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.Action;
import com.myapp.worktrack_backend.registration.auth.AuthorizationService;
import com.myapp.worktrack_backend.registration.auth.RegistrationApprovalContext;
import com.myapp.worktrack_backend.registration.dto.ApprovalRequestDTO;
import com.myapp.worktrack_backend.registration.dto.EmployeeCreationDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.*;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.security.TemporaryPasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationStateService {
    private final EmployeeRepo emprepo;
    private final RegistrationRepo registrepo;
    private final PasswordEncoder passwordEncoder;
    private final LoginRepo loginRepo;
    private final AuthorizationService authorizationService;
    private final DepartmentRepo departmentRepo;
    private final WorkspaceRepo workspaceRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;

    public ApplicationStateService(EmployeeRepo emprepo, RegistrationRepo registRepo, PasswordEncoder passwordEncoder, LoginRepo loginRepo, AuthorizationService authorizationService, DepartmentRepo departmentRepo, WorkspaceRepo workspaceRepo, EmployeeWorkspaceRepo empWorkspaceRepo) {
        this.emprepo=emprepo;
        this.registrepo=registRepo;
        this.passwordEncoder=passwordEncoder;
        this.loginRepo=loginRepo;
        this.authorizationService=authorizationService;
        this.departmentRepo=departmentRepo;
        this.workspaceRepo=workspaceRepo;
        this.employeeWorkspaceRepo=empWorkspaceRepo;
    }

    @Transactional
    public EmployeeCreationDTO employeeApproval(ApprovalRequestDTO approvalRequestDTO, String registUUID) {
        //get the registration by registration UUID
        Registration regisRec = registrepo.findByRegistrationUUIDForUpdate(registUUID)
                .orElseThrow(() -> new RegistrationNumberNotFoundException("Registration Not found!"));

        //change the DTO to record to pass for authorization.
        RegistrationApprovalContext regAppCont = new RegistrationApprovalContext(approvalRequestDTO.getRole(), approvalRequestDTO.getDepartmentId(), approvalRequestDTO.getWorkspaceId());

        //perform the authorization and state check
        authorizationService.authorize(Action.approve_registration, regisRec, null, regAppCont);

        System.out.println("Registration authorization passed!");
        //Before employee creation and authorization successfully check for other validation.
        //1- check and department and workspace are valid and are in Active state
        //check workspace amd status
        Workspace verifiedWorkspace = workspaceRepo.findById(approvalRequestDTO.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFound("Workspace Not Found"));
        if(!"Active".equalsIgnoreCase(verifiedWorkspace.getStatus())) {
            throw new WorkspaceIsInactiveException("Workspace is InActive");
        }
        DepartmentWorkspace departmentWorkspace = new DepartmentWorkspace();
        departmentWorkspace.setWId(verifiedWorkspace.getWId());
        departmentWorkspace.setDeptId(approvalRequestDTO.getDepartmentId());
        Department department = departmentRepo.findByIdForUpdate(departmentWorkspace)
                .orElseThrow(() -> new DepartmentNotFoundException("Department Not Found"));

        if(!"Active".equalsIgnoreCase(department.getStatus())) {
            throw new DepartmentIsInactiveException("Department is InActive");
        }
        //2- seat capacity in department at the time of approval
        //get count of employees in the department
        long currentDeptCount = employeeWorkspaceRepo.countWorkDeptEmployees(department.getDepartmentwkspc().getDeptId(), verifiedWorkspace.getWId());

        if(department.getCapacity() == null || department.getCapacity() <= 0) {
            throw new IllegalArgumentException("Department capacity not configured");
        }
        if(currentDeptCount >= department.getCapacity()) {
            throw new DepartmentCapacityExceeded("Department capacity is full");
        }
        if(department.getDepartmentHead() == null) {
            throw new DepartmentHeadNotDefined("Department currently unoperational");
        }
        Employee deptHead = department.getDepartmentHead();
        if(!"Active".equalsIgnoreCase(deptHead.getEmployeeStatus())) {
            throw new EmployeeExpiredException("Department Head is not Active");
        }
//        //check if the workspace is same and they are admin
//        List<Integer> deptHeadWorkspaces = emprepo.getEmpWorkspaceByEmpId(deptHead.getEmpId())
//                .orElseThrow(() -> new WorkspaceNotFound("Workspace not found!"));
//        Boolean eligibleDepartmentHead=false;
//        for(Integer w: deptHeadWorkspaces) {
//            if("Admin".equals(deptHead.getRole()) && w == verifiedWorkspace.getWId()) {
//               eligibleDepartmentHead=true;
//            }
//        }
//        if(!eligibleDepartmentHead) {
//            throw new DeptHeadOutOfScope("Department Head out of scope");
//        }

        if(!"Admin".equalsIgnoreCase(deptHead.getRole()) || employeeWorkspaceRepo.getCountOfDeptHeadWorkspaces(deptHead.getEmpId(), verifiedWorkspace.getWId()) == 0) {
            throw new DepartmentHeadInEligible("Department Head is ineligible to perform the action");
        }


        //create employee and set role to active employee
        Employee e = new Employee();
        e.setRegId(regisRec.getRegID());
        e.setRole(approvalRequestDTO.getRole());
        e.setEmployeeStatus("Active");

        //Now an active employee registration also approved
        regisRec.setApplicationStatus("Approved");
        regisRec.setRole(approvalRequestDTO.getRole());
        //create temp empID before save
        String tmpID = UUID.randomUUID().toString();
        e.setEmpId("TMP"+ tmpID.substring(0, 5)); //temporarily
        Employee savedEmp = emprepo.save(e);
        String empId = String.format("EM%06d", savedEmp.getEmployeeId());
        savedEmp.setEmpId(empId);
        Employee finalEmp = emprepo.save(savedEmp);

        //once employee is created also add them to their respective workspace
        EmpWorkspaceId empWorkspaceId = new EmpWorkspaceId();
         empWorkspaceId.setEmpId(finalEmp.getEmpId());
         empWorkspaceId.setWorkSpaceId(verifiedWorkspace.getWId());
        EmpWorkspace empWorkspace = new EmpWorkspace();
        empWorkspace.setEmpWorkspaceId(empWorkspaceId);
        empWorkspace.setDeptId(department.getDepartmentwkspc().getDeptId());
        employeeWorkspaceRepo.save(empWorkspace);


        //create login credentials
        Login newUser = new Login();
        newUser.setEmployee(finalEmp);
        newUser.setWorkEmail(regisRec.getWorkEmail());
        //encode the password generate the hash and store hash in db
        //raw password
        String tempPassword = TemporaryPasswordGenerator.generate(12);
        String hashedPassword = passwordEncoder.encode(tempPassword);
        newUser.setPasswordHash(hashedPassword);
        newUser.setMustChangePassword(true);

        loginRepo.save(newUser);
        registrepo.save(regisRec);
        //Structure response in DTO
        EmployeeCreationDTO ecdto = new EmployeeCreationDTO();
        ecdto.setEmpId(finalEmp.getEmpId());
        ecdto.setWorkEmail(newUser.getWorkEmail());
        ecdto.setPassword(tempPassword);
        ecdto.setEmpId(empId);
        ecdto.setMessage("Employee created successfully");
        return ecdto;
    }

    @Transactional
    public String registrationRejection(String registrationUUID) {
        Registration regRec = registrepo.findByRegistrationUUIDForUpdate(registrationUUID)
                .orElseThrow(() -> new RegistrationNumberNotFoundException("Registration Not found."));

        if(!"Pending".equals(regRec.getApplicationStatus())) {
            throw new InvalidRegistrationStateException("Invalid Registration state");
        }

        authorizationService.authorize(Action.reject_registration, regRec, null, null);
        regRec.setApplicationStatus("Rejected");
        registrepo.save(regRec);
        return "Application Rejected successfully";
    }
}
