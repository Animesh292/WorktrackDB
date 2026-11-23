package com.myapp.worktrack_backend.registration.auth;

import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Registration;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.PermanentSuperAdminRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;


@Component
public class EmployeeLifecyclePolicies {
    private final AuthenticatedEmployeeDetails authEmpDetails;
    private final PermanentSuperAdminRepo permanentSuperAdminRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;

    public EmployeeLifecyclePolicies(AuthenticatedEmployeeDetails authEmpDetails, PermanentSuperAdminRepo permanentSuperAdminRepo, EmployeeWorkspaceRepo employeeWorkspaceRepo) {
        this.authEmpDetails=authEmpDetails;
        this.permanentSuperAdminRepo=permanentSuperAdminRepo;
        this.employeeWorkspaceRepo=employeeWorkspaceRepo;
    }
    public void suspendEmployee(Employee targetEmployee) {
        //get Actor Role this also validates whether they are active internally
        if(!"Active".equals(targetEmployee.getEmployeeStatus())) {
            throw new AccessDeniedException("Invalid lifecycle transition");
        }
        Employee actorEmployee = authEmpDetails.getAuthEmployeeDetails();
        //check the roles are genuine or not for both
       authorizeEmployeeLifecycleAction(targetEmployee, actorEmployee);
    }

    public void activateEmployee(Employee targetEmployee) {
        if(!"Suspended".equals(targetEmployee.getEmployeeStatus())) {
            throw new AccessDeniedException("Invalid lifecycle transition");
        }
        Employee actorEmployee = authEmpDetails.getAuthEmployeeDetails();
        authorizeEmployeeLifecycleAction(targetEmployee, actorEmployee);
    }
    public void terminateEmployee(Employee targetEmployee) {

        if("Terminated".equals(targetEmployee.getEmployeeStatus())) {
            throw new AccessDeniedException("Invalid lifecycle transition");
        }
        Employee actorEmployee = authEmpDetails.getAuthEmployeeDetails();
        authorizeEmployeeLifecycleAction(targetEmployee, actorEmployee);
    }
    public void approveRegistration(Registration registration, RegistrationApprovalContext regContext) {
        //state validation check only if the registration is pending it can be approved
        if(!"Pending".equals(registration.getApplicationStatus())) {
            throw new AccessDeniedException("Action denied application already approved");
        }

        //get the authenticated actor details
        Employee actorEmployee = authEmpDetails.getAuthEmployeeDetails();
        //once we get the authenticated actor details we'll check the authorizaion pass in our helper method
        authorizeRegistration(registration, actorEmployee, regContext);
    }
    public void rejectRegistration(Registration registration, RegistrationApprovalContext regContext) {
        if(!"Pending".equals(registration.getApplicationStatus())) {
            throw new AccessDeniedException("Invalid state to perform action");
        }
        Employee actorEmployee = authEmpDetails.getAuthEmployeeDetails();
        if ("Staff".equals(actorEmployee.getRole())) {
            throw new AccessDeniedException("Insufficient permission to perform this action");
        }

        String targetRole = registration.getRole();

        // 1. Super-Admin applicant rejection: requires Permanent Super-Admin authority
        if ("Super-Admin".equals(targetRole)) {
            if (!"Super-Admin".equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Insufficient rights to perform the action");
            }
            if (!permanentSuperAdminRepo.existsByEmployeeEmployeeId(actorEmployee.getEmployeeId())) {
                throw new AccessDeniedException("Privileged access denied needs permanent authority to perform the action");
            }
            return;
        }

        // 2. Admin applicant rejection: requires Super-Admin authority (Admin cannot reject Admin)
        if ("Admin".equals(targetRole)) {
            if (!"Super-Admin".equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Permission denied cannot act on same or higher role. Privileged access required");
            }
            return;
        }

        // 3. Staff applicant rejection: Super-Admin has global scope; Admin is strictly scoped to assigned (workspace, dept)
        if ("Super-Admin".equals(actorEmployee.getRole())) {
            return;
        }

        if ("Admin".equals(actorEmployee.getRole())) {
            if (employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                    actorEmployee.getEmpId(), registration.getWorkspaceId(), registration.getDepartmentId()) == 0) {
                throw new AccessDeniedException("Insufficient rights to reject outside assigned department scope");
            }
            return;
        }

        throw new AccessDeniedException("Permission denied for invalid role");
    }
    public void changeRole(Employee targetEmployee, String requestedRole) {
        //chekc the authEmp Details -> checks if active internally
        Employee actorDetails = authEmpDetails.getAuthEmployeeDetails();

        //chcek if roles are valid actor, target and requestedRole
        String[] roles = {"Staff", "Admin", "Super-Admin"};
        boolean isActorRoleValid=false, isTargetRoleValid=false, isRequestedRoleValid=false;
        for(String role: roles) {
            if(role.equals(targetEmployee.getRole())) {
                isTargetRoleValid=true;
            }
            if(role.equals(actorDetails.getRole())) {
                isActorRoleValid=true;
            }
            if(role.equals(requestedRole)) {
                isRequestedRoleValid=true;
            }
        }

        if(!isTargetRoleValid || !isActorRoleValid || !isRequestedRoleValid) {
            throw new AccessDeniedException("Action not permitted for invalid roles");
        }

        //check if self-action role as not permitted - but it will be rejected for both super=admins a deep bottleneck
        if(actorDetails.getEmployeeId() == targetEmployee.getEmployeeId() || targetEmployee.getRole().equals(requestedRole)) {
            throw new AccessDeniedException("Self-action and no-op role changes are denied");
        }

        //core businees policy rules
        if("Staff".equals(actorDetails.getRole())) {
            throw new AccessDeniedException("Access denied for action");
        }
        String targetEmployeeRole = targetEmployee.getRole(); // shuld be staff, admin
        if(!"Super-Admin".equals(targetEmployee.getRole())) {
            //same role not possible since we already checked.
            if(actorDetails.getRole().equals(targetEmployeeRole)) {
                throw new AccessDeniedException("Action not permitted for same role");
            }
            //possible staff
             //only can be promoted to admin by admin actor
                if ("Staff".equals(targetEmployeeRole) && "Admin".equals(actorDetails.getRole()) && "Admin".equals(requestedRole)) {
                    if (employeeWorkspaceRepo.isScopeValid(targetEmployee.getEmpId(), actorDetails.getEmpId()) == 0) {
                        throw new AccessDeniedException("Action not permitted outside scope");
                    }
                    //can promote
                    return;
                }
                //if actor is super-admin in that case for promotion and demotion are possible.
            else if(("Staff".equals(targetEmployee.getRole()) || "Admin".equals(targetEmployee.getRole())) && "Super-Admin".equals(actorDetails.getRole())){
                if ("Super-Admin".equals(requestedRole)
                        && !permanentSuperAdminRepo.existsByEmployeeEmployeeId(actorDetails.getEmployeeId())) {
                    throw new AccessDeniedException("Permanent authority is required to assign Super-Admin role");
                }
                return;//both promotion/demotion allowed by super-admin
            }
        }
        else {
            if(!"Super-Admin".equals(actorDetails.getRole())) {
                throw new AccessDeniedException("Access denied");
            }
            if(!permanentSuperAdminRepo.existsByEmployeeEmployeeId(actorDetails.getEmployeeId())) {
                throw new AccessDeniedException("Insufficient rights");
            }
            if (permanentSuperAdminRepo.existsByEmployeeEmployeeId(targetEmployee.getEmployeeId())) {
                throw new AccessDeniedException("Permanent Super-Admin status cannot be changed through role management");
            }
            return; //can demote super-admin target employee
        }
        throw new AccessDeniedException("Invalid Operation");
    }

    private void authorizeEmployeeLifecycleAction(Employee targetEmployee, Employee actorEmployee) {
        String[] roles = {"Staff", "Admin", "Super-Admin"};
        boolean actorRolePresent = false; boolean targetRolePresent = false;
        for(String role: roles) {
            if(role.equals(actorEmployee.getRole())) {
                actorRolePresent=true;
            }
            if(role.equals(targetEmployee.getRole())) {
                targetRolePresent = true;
            }
        }
        if(!actorRolePresent || !targetRolePresent) {
            throw new AccessDeniedException("Action not permitted for invalid roles");
        }
        // check whether the actor is not actioning on himself.
        if(actorEmployee.getEmployeeId() == targetEmployee.getEmployeeId()) {
            throw new AccessDeniedException("Action can'be be completed");
        }
        if("Staff".equals(actorEmployee.getRole())) {
            throw new AccessDeniedException("Action not permitted in this role");
        }
        //now Actor admin, super-admin check Role of target employee
        String targetRole = targetEmployee.getRole();
        if(!"Super-Admin".equals(targetRole)) { //admin, staff
            //if roles are same not actionable
            if(targetRole.equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Action not permitted in this role");
            }

            if("Staff".equals(targetRole) && "Admin".equals(actorEmployee.getRole())) {
                //check scope
                long countSharedWorkspace = employeeWorkspaceRepo.isScopeValid(targetEmployee.getEmpId(), actorEmployee.getEmpId());
                if(countSharedWorkspace == 0) {
                    throw new AccessDeniedException("Insufficient rights to perform this action");
                }
                return;
            }
            else if(("Admin".equals(targetRole) || "Staff".equals(targetRole)) && "Super-Admin".equals(actorEmployee.getRole())) {
                //actionable no scope checking - allow
                return;
            }
        }
        else { // it means they are super-admin check if actor is permanent super admin
            if(!"Super-Admin".equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Action not permitted in this role");
            }
            if(!permanentSuperAdminRepo.existsByEmployeeEmployeeId(actorEmployee.getEmployeeId())) { // not a permanent super-admin
                throw new AccessDeniedException("Insufficient rights to perform this action");
            }
            return;
        }
        throw new AccessDeniedException("Action not permitted");
    }

    private void authorizeRegistration(Registration registration,Employee actorEmployee, RegistrationApprovalContext regContext) {
        if (regContext == null) {
            throw new AccessDeniedException("Approval assignment is required");
        }
        //check if the actor has a valid role
        String[] roles = {"Admin", "Staff", "Super-Admin"};
        boolean actorRoleIsValid=false, targetRoleIsValid=false;
        for(String role: roles) {
            if(role.equals(actorEmployee.getRole())) {
                actorRoleIsValid=true;
            }
            if(role.equals(regContext.role())) {
                targetRoleIsValid=true;
            }
        }
        if(!actorRoleIsValid || !targetRoleIsValid) {
            throw new AccessDeniedException("Permission Denied Invalid role");
        }

        //staff role cannot act
        if("Staff".equals(actorEmployee.getRole())) {
            throw new AccessDeniedException("Insufficient permission to perform this action");
        }
        //actor can be admin, super-admin, registration role can be anything staff, admin, super-admin
        //since the registered applicant is not an employee yet then have to approved first for approval workflow to convert to an employee
        //but a super-admin applicant cannot be actioned by lower role-role hiearchy must follow for authorization
        if(!"Super-Admin".equals(regContext.role())) {
            //if both are admin (role check is already done so no arbitary roles can match
            if(regContext.role().equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Permission denied cannot act on same role. Privileged access required");
            }
            //if role are different then target is staff, admin and the acting employee can be super-admin, admin
            if("Staff".equals(regContext.role()) && "Admin".equals(actorEmployee.getRole())) {
                if(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(actorEmployee.getEmpId(), regContext.workspaceId(), regContext.departmentId()) == 0) {
                    throw new AccessDeniedException("Insufficient rights to approve outside assigned scope");
                }
                return;
            }
            else if(("Staff".equals(regContext.role()) || "Admin".equals(regContext.role())) && "Super-Admin".equals(actorEmployee.getRole())) {
                return;
            }
        }
        else { // they are super-admin applicants
            if(!"Super-Admin".equals(actorEmployee.getRole())) {
                throw new AccessDeniedException("Insufficient rights to perform the action");
            }
            if(!permanentSuperAdminRepo.existsByEmployeeEmployeeId(actorEmployee.getEmployeeId())) {
                throw new AccessDeniedException("Privileged access denied needs permanent authority to perform the action");
            }
            return;
        }
        throw new AccessDeniedException("Invalid Operation");
    }
}
