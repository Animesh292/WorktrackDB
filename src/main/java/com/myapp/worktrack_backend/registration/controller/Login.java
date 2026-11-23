package com.myapp.worktrack_backend.registration.controller;
import com.myapp.worktrack_backend.registration.dto.LoginRequestDTO;
import com.myapp.worktrack_backend.registration.dto.LoginResponseDTO;
import com.myapp.worktrack_backend.registration.dto.ChangePasswordRequestDTO;
import com.myapp.worktrack_backend.registration.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class Login {
    private final LoginService loginService;

    public Login(LoginService loginService) {
        this.loginService=loginService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@jakarta.validation.Valid @RequestBody ChangePasswordRequestDTO request) {
        loginService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> loginUser(@jakarta.validation.Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loggedInResponse = loginService.loginUser(loginRequestDTO);
        return new ResponseEntity<>(loggedInResponse, HttpStatus.OK);
    }

}
