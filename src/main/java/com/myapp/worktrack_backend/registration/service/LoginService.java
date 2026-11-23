package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.JWTService;
import com.myapp.worktrack_backend.registration.dto.LoginRequestDTO;
import com.myapp.worktrack_backend.registration.dto.LoginResponseDTO;
import com.myapp.worktrack_backend.registration.dto.ChangePasswordRequestDTO;
import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Login;
import com.myapp.worktrack_backend.registration.exceptionhandler.AccountDeactivated;
import com.myapp.worktrack_backend.registration.exceptionhandler.IncorrectPassword;
import com.myapp.worktrack_backend.registration.exceptionhandler.LoginNotFound;
import com.myapp.worktrack_backend.registration.repository.LoginRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final LoginRepo loginRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticatedEmployeeDetails authenticatedEmployeeDetails;

    public LoginService(LoginRepo loginRepo, PasswordEncoder passwordEncoder, JWTService jwtService, AuthenticatedEmployeeDetails authenticatedEmployeeDetails) {
        this.loginRepo=loginRepo;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
        this.authenticatedEmployeeDetails=authenticatedEmployeeDetails;
    }

    public void changePassword(ChangePasswordRequestDTO request) {
        Employee employee = authenticatedEmployeeDetails.getAuthEmployeeDetails();
        Login login = loginRepo.findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElseThrow(() -> new LoginNotFound("Login not found."));
        if (!passwordEncoder.matches(request.currentPassword(), login.getPasswordHash())) {
            throw new IncorrectPassword("Current password is incorrect.");
        }
        login.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        login.setMustChangePassword(false);
        loginRepo.save(login);
    }

    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        Login loggedIn = loginRepo.findByWorkEmail(loginRequestDTO.getWork_email())
                .orElseThrow(() -> new LoginNotFound("Invalid username or Password."));
        Employee e = loggedIn.getEmployee();
        if(!"Active".equals(e.getEmployeeStatus())) {
            throw new AccountDeactivated("Your account has been deactivated.");
        }
        //check if password is valid
        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), loggedIn.getPasswordHash())) {
            throw new IncorrectPassword("Invalid username or Password.");
        }
        //User is authenticated generate token
        String token = jwtService.generateToken(e.getEmployeeId());
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setRole(e.getRole());
        loginResponseDTO.setMustChangePassword(loggedIn.getMustChangePassword()); //1 temp login password
        loginResponseDTO.setToken(token);
        return loginResponseDTO;
    }

}
