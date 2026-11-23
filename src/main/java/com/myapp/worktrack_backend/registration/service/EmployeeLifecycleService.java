package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.Action;
import com.myapp.worktrack_backend.registration.auth.AuthorizationService;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeLifecycleService {
    private final AuthorizationService authorizationService;
    private final EmployeeRepo employeeRepo;

    public EmployeeLifecycleService(AuthorizationService authorizationService, EmployeeRepo employeeRepo) {
        this.authorizationService = authorizationService;
        this.employeeRepo = employeeRepo;
    }

    @Transactional
    public void employeeSuspensionService(String empId) {
        Employee targetEmployee = employeeRepo.findByEmpId(empId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        authorizationService.authorize(Action.suspend_employee, targetEmployee, null, null);
        targetEmployee.setEmployeeStatus("Suspended");
        employeeRepo.save(targetEmployee);
    }

    @Transactional
    public void employeeActivation(String empId) {
        Employee targetEmployee = employeeRepo.findByEmpId(empId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee doesn't exist"));
        authorizationService.authorize(Action.activate_employee, targetEmployee, null, null);
        targetEmployee.setEmployeeStatus("Active");
        employeeRepo.save(targetEmployee); // explicit save — do not rely solely on dirty-checking
    }

    @Transactional
    public void employeeTermination(String empId) {
        Employee targetEmployee = employeeRepo.findByEmpId(empId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee doesn't exist"));
        authorizationService.authorize(Action.terminate_employee, targetEmployee, null, null);
        targetEmployee.setEmployeeStatus("Terminated");
        employeeRepo.save(targetEmployee);
        // EmpWorkspace rows are intentionally preserved — termination must not delete assignment history
    }

    /**
     * Changes the role of a target employee.
     * Actor must be authenticated (JWT). Authorization is fully delegated to
     * AuthorizationService -> EmployeeLifecyclePolicies.changeRole().
     *
     * @param targetEmpId   business string ID of the employee whose role is being changed
     * @param requestedRole the new role proposed by the actor (Staff | Admin | Super-Admin)
     */
    @Transactional
    public void changeRole(String targetEmpId, String requestedRole) {
        Employee targetEmployee = employeeRepo.findByEmpId(targetEmpId)
                .orElseThrow(() -> new EmployeeNotFoundException("Target employee not found"));
        authorizationService.authorize(Action.change_role, targetEmployee, requestedRole, null);
        targetEmployee.setRole(requestedRole);
        employeeRepo.save(targetEmployee);
    }
}
