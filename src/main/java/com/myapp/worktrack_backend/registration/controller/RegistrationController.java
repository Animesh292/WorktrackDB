package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.RegistrationDTO;
import com.myapp.worktrack_backend.registration.dto.RegistrationResponseDTO;
import com.myapp.worktrack_backend.registration.dto.RegistrationTrackingResponseDTO;
import com.myapp.worktrack_backend.registration.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("register")
public class RegistrationController {

    private final RegistrationService regService;

    public RegistrationController(RegistrationService regsService) {
        this.regService=regsService;
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@jakarta.validation.Valid @RequestBody RegistrationDTO register) {
            RegistrationResponseDTO res = regService.register(register);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
    
    @GetMapping("/{registrationID}")
    public ResponseEntity<?> trackRegistration(@PathVariable String registrationID) {
        RegistrationTrackingResponseDTO rtrdto = regService.registrationTracking(registrationID);
        return new ResponseEntity<>(rtrdto, HttpStatus.OK);
    }
}
