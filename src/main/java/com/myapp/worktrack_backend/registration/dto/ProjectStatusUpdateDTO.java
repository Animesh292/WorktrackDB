package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for a project status transition.
 */
public record ProjectStatusUpdateDTO(

        @NotBlank(message = "New status must not be blank")
        @Pattern(
            regexp = "DRAFT|ACTIVE|COMPLETED|CANCELLED",
            message = "Status must be one of: DRAFT, ACTIVE, COMPLETED, CANCELLED"
        )
        String newStatus
) {}
