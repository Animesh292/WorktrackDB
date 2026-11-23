package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for the Change Role endpoint.
 * The actor (authenticated via JWT) proposes a new role for the target employee
 * identified by empId in the URL path.
 */
public record ChangeRoleRequestDTO(
        @NotBlank(message = "Requested role must not be blank")
        @Pattern(regexp = "Staff|Admin|Super-Admin", message = "Role must be one of: Staff, Admin, Super-Admin")
        String requestedRole
) {}
