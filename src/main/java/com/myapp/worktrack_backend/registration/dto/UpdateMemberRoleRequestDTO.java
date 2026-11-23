package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO to update a project member's role.
 */
public record UpdateMemberRoleRequestDTO(

        @NotBlank(message = "New role is required")
        @Pattern(regexp = "^(OWNER|MANAGER|CONTRIBUTOR|VIEWER)$",
                message = "Role must be one of: OWNER, MANAGER, CONTRIBUTOR, VIEWER")
        String role
) {}
