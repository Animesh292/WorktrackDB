package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for updating an existing Project's metadata.
 * All fields are optional — only non-null values are applied.
 */
public record UpdateProjectRequestDTO(

        @Size(max = 255, message = "Project name must be 255 characters or less")
        String name,

        @Size(max = 2000, message = "Description must be 2000 characters or less")
        String description,

        LocalDate startDate,
        LocalDate targetEndDate,
        LocalDate actualCompletionDate
) {}
