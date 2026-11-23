package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO to add an employee as a member to a project.
 */
public record AddProjectMemberRequestDTO(

        @NotBlank(message = "Employee ID (empId) is required")
        String empId,

        @NotBlank(message = "Project member role is required")
        @Pattern(regexp = "^(OWNER|MANAGER|CONTRIBUTOR|VIEWER)$",
                message = "Role must be one of: OWNER, MANAGER, CONTRIBUTOR, VIEWER")
        String role
) {}
