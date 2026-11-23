package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for creating a new Project.
 */
public record CreateProjectRequestDTO(

        @NotBlank(message = "Project key must not be blank")
        @Size(max = 50, message = "Project key must be 50 characters or less")
        @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Project key may only contain letters, numbers, hyphens and underscores")
        String projectKey,

        @NotBlank(message = "Project name must not be blank")
        @Size(max = 255, message = "Project name must be 255 characters or less")
        String name,

        @Size(max = 2000, message = "Description must be 2000 characters or less")
        String description,

        @NotNull(message = "Workspace ID is required")
        Integer workspaceId,

        @NotBlank(message = "Department ID is required")
        @Size(max = 5, message = "Department ID must be 5 characters or less")
        String deptId,

        LocalDate startDate,
        LocalDate targetEndDate
) {}
