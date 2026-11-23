package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.ChangeRoleRequestDTO;
import com.myapp.worktrack_backend.registration.service.EmployeeLifecycleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee-action")
public class EmployeeLifecycleController {
    private final EmployeeLifecycleService employeeLifecycleService;

    public EmployeeLifecycleController(EmployeeLifecycleService employeeLifecycleService) {
        this.employeeLifecycleService = employeeLifecycleService;
    }

    @PostMapping("/suspend/{empId}")
    public ResponseEntity<?> suspendEmployee(@PathVariable String empId) {
        employeeLifecycleService.employeeSuspensionService(empId);
        return ResponseEntity.ok("Employee suspended successfully");
    }

    @PostMapping("/terminate/{empId}")
    public ResponseEntity<?> terminateEmployee(@PathVariable String empId) {
        employeeLifecycleService.employeeTermination(empId);
        return ResponseEntity.ok("Employee terminated successfully");
    }

    @PostMapping("/activate/{empId}")
    public ResponseEntity<?> activateEmployee(@PathVariable String empId) {
        employeeLifecycleService.employeeActivation(empId);
        return ResponseEntity.ok("Employee activated successfully");
    }

    /**
     * POST /employee-action/change-role/{empId}
     * Body: { "requestedRole": "Admin" }
     *
     * The authenticated actor proposes a role change for the target employee (empId).
     * Authorization is enforced by EmployeeLifecyclePolicies.changeRole().
     */
    @PostMapping("/change-role/{empId}")
    public ResponseEntity<?> changeRole(
            @PathVariable String empId,
            @Valid @RequestBody ChangeRoleRequestDTO request) {
        employeeLifecycleService.changeRole(empId, request.requestedRole());
        return ResponseEntity.ok("Employee role updated successfully");
    }
}
