package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDate;


@Data
public class RegistrationDTO {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email
    private String workEmail;
    @NotBlank private String phoneNumber;
    @NotBlank private String gender;
    private String workspace;
    @NotNull
    @Positive
    private Integer workspaceId;

    private String department;
    @NotBlank
    private String departmentId;
    @PositiveOrZero private float experience;
    @NotNull private LocalDate birthDate;
}
