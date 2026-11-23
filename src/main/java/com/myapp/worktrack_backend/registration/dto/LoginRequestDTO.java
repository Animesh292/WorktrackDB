package com.myapp.worktrack_backend.registration.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequestDTO {
    @NotBlank @Email String work_email;
    @NotBlank String password;
}
