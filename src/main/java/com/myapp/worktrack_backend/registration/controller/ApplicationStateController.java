package com.myapp.worktrack_backend.registration.controller;


import com.myapp.worktrack_backend.registration.dto.ApprovalRequestDTO;
import com.myapp.worktrack_backend.registration.dto.EmployeeCreationDTO;
import com.myapp.worktrack_backend.registration.service.ApplicationStateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class ApplicationStateController {
    private final ApplicationStateService empApproval;

    public ApplicationStateController(ApplicationStateService empApproval) {
        this.empApproval=empApproval;
    }

    @PostMapping("/approve/{registrationUUID}")
    public ResponseEntity<?> approveEmployee(@jakarta.validation.Valid @RequestBody ApprovalRequestDTO approvalRequest, @PathVariable String registrationUUID) {
        EmployeeCreationDTO ecrdto = empApproval.employeeApproval(approvalRequest, registrationUUID);
        return new ResponseEntity<>(ecrdto, HttpStatus.ACCEPTED);
    }

    @PostMapping("/reject/{registrationUUID}")
    public ResponseEntity<?> rejectEmployee(@PathVariable String registrationUUID) {
        String rejection = empApproval.registrationRejection(registrationUUID);
        return new ResponseEntity<>(rejection, HttpStatus.ACCEPTED);
    }
}
