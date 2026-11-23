package com.myapp.worktrack_backend.registration.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String role;
    private boolean mustChangePassword;
    private String token;
}
