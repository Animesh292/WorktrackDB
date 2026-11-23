package com.myapp.worktrack_backend.registration.auth;

import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Registration;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationService {
    private final EmployeeLifecyclePolicies empPolicies;
    public AuthorizationService(EmployeeLifecyclePolicies empPolicies) {
        this.empPolicies=empPolicies;
    }

    public void authorize(Action action, AuthorizableResource target, String requestedRole, RegistrationApprovalContext registrationApprovalContext) {
            switch(action) {
                case activate_employee:
                    if(!(target instanceof Employee employee)) {
                        throw new IllegalArgumentException("Employee target required");
                    }
                    empPolicies.activateEmployee(employee);
                    break;
                case suspend_employee:
                    if(!(target instanceof Employee employee)) {
                        throw new IllegalArgumentException("Employee target required");
                    }
                    empPolicies.suspendEmployee(employee);
                    break;
                case terminate_employee:
                    if(!(target instanceof Employee employee)) {
                        throw new IllegalArgumentException("Employee target required");
                    }
                    empPolicies.terminateEmployee(employee);
                    break;
                case approve_registration:
                    if(!(target instanceof Registration registration)) {
                        throw new IllegalArgumentException("Registration target required");
                    }
                    empPolicies.approveRegistration(registration, registrationApprovalContext);
                    break;
                case reject_registration:
                    if(!(target instanceof Registration registration)) {
                        throw new IllegalArgumentException("Registration target required");
                    }
                    empPolicies.rejectRegistration(registration, registrationApprovalContext);
                    break;
                case change_role:
                    if(!(target instanceof Employee employee)) {
                        throw new IllegalArgumentException("Employee target required");
                    }
                    empPolicies.changeRole(employee, requestedRole);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid authorization action");

            }
    }
}
