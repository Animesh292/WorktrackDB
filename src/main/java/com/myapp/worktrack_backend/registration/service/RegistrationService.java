package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.dto.RegistrationDTO;
import com.myapp.worktrack_backend.registration.dto.RegistrationResponseDTO;
import com.myapp.worktrack_backend.registration.dto.RegistrationTrackingResponseDTO;
import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.DepartmentWorkspace;
import com.myapp.worktrack_backend.registration.entity.Registration;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import com.myapp.worktrack_backend.registration.exceptionhandler.*;
import com.myapp.worktrack_backend.registration.repository.DepartmentRepo;
import com.myapp.worktrack_backend.registration.repository.RegistrationRepo;
import com.myapp.worktrack_backend.registration.repository.WorkspaceRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class RegistrationService {
    private final RegistrationRepo registrationRepo;
    private final WorkspaceRepo workspaceRepo;
    private final DepartmentRepo departmentRepo;

    public RegistrationService(RegistrationRepo registrationRepo, WorkspaceRepo workspaceRepo, DepartmentRepo departmentRepo) {
        this.registrationRepo = registrationRepo;
        this.workspaceRepo = workspaceRepo;
        this.departmentRepo = departmentRepo;
    }
    @Transactional
   public RegistrationResponseDTO register(RegistrationDTO regDTO) {
       //duplicate check
        if(registrationRepo.existsByWorkEmail(regDTO.getWorkEmail())) {
            throw new DuplicateRegistrationException("Duplicate Registration the email "+ regDTO.getWorkEmail()+ " already exists!");
        }
       // other validation checks
       //1- Get the Workspace and state check
       Workspace verifiedWorkspace = workspaceRepo.findById(regDTO.getWorkspaceId())
               .orElseThrow(() -> new WorkspaceNotFound("Workspace doesn't exist"));

       if(!"Active".equals(verifiedWorkspace.getStatus())) {
          throw new WorkspaceIsInactiveException("Workspace is inActive");
       }
        //2- Get Department from the deptID and WId
       DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setDeptId(regDTO.getDepartmentId());
       dw.setWId(verifiedWorkspace.getWId());

       Department department = departmentRepo.findById(dw)
               .orElseThrow(() -> new DepartmentNotFoundException("No Department Found"));

       if(!"Active".equalsIgnoreCase(department.getStatus())) {
           throw new DepartmentIsInactiveException("Department is inActive");
       }

        //random registration ID generator
       UUID random = UUID.randomUUID();

        Registration reg = new Registration();
//       if(registrationRepo.existsByDepartment(regDTO.getDepartment()) || registrationRepo.existsByWorkspace(regDTO.getWorkspace())) {
//           return Map.of("isDuplicateDeptOrWork", true);
//       }

       //final registration
       reg.setWorkEmail(regDTO.getWorkEmail());
       reg.setFirstName(regDTO.getFirstName());
       reg.setLastName(regDTO.getLastName());
       reg.setSex(Character.toUpperCase(regDTO.getGender().charAt(0)));
       reg.setPhnNum(regDTO.getPhoneNumber());
       reg.setWorkspaceId(verifiedWorkspace.getWId());
       reg.setDepartmentId(department.getDepartmentwkspc().getDeptId());
       reg.setExperience(regDTO.getExperience());
       reg.setBirthdate(regDTO.getBirthDate());
       reg.setRole(null);
       reg.setApplicationStatus("Pending");
       reg.setRegistrationUUID(random.toString());
       //save in db
       registrationRepo.save(reg);

       RegistrationResponseDTO rrd = new RegistrationResponseDTO();
       rrd.setMessage("Registration successful");
       rrd.setRegistrationUUID(random.toString());
       return rrd;
   }

   public RegistrationTrackingResponseDTO registrationTracking(String registrationUUID) {
        Registration regisUUID = registrationRepo.findByRegistrationUUID(registrationUUID)
       .orElseThrow(() -> new RegistrationNumberNotFoundException("Registration not found"));
        RegistrationTrackingResponseDTO rgtrdto = new RegistrationTrackingResponseDTO();
        rgtrdto.setRegistrationUUID(regisUUID.getRegistrationUUID());
        rgtrdto.setStatus(regisUUID.getApplicationStatus());
        return rgtrdto;
   }
}
