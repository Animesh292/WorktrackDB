package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for assigning a Department Head (HOD).
 * The empId identifies the employee to appoint as HOD.
 * Workspace and department are provided via the URL path.
 */
public record AssignDeptHeadRequestDTO(
        @NotBlank(message = "Employee ID must not be blank")
        String empId
) {}
