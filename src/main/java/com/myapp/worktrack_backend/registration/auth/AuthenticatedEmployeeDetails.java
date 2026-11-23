package com.myapp.worktrack_backend.registration.auth;


import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeExpiredException;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedEmployeeDetails {
    private final EmployeeRepo empRepo;
    private final CurrentEmployee currentEmployee;

    public AuthenticatedEmployeeDetails(EmployeeRepo empRepo, CurrentEmployee currentEmployee) {
        this.empRepo=empRepo;
        this.currentEmployee=currentEmployee;
    }

    public Employee getAuthEmployeeDetails() {
        Employee emp = empRepo.findById(currentEmployee.getCurrentEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found!"));
        if(!"Active".equals(emp.getEmployeeStatus())) {
            throw new EmployeeExpiredException("Account is expired");
        }
        return emp;
    }

    public String getAuthEmpRole() {
        return getAuthEmployeeDetails().getRole();
    }
}
