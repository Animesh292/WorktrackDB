package com.myapp.worktrack_backend.registration.dto;

import lombok.Data;

@Data
public class EmployeeCreationDTO {
    String empId;
    String message;
    String workEmail;
    String password;
}
