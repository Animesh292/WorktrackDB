package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateSprintRequestDTO(
        @Size(max = 100, message = "Sprint name must not exceed 100 characters")
        String name,

        String goal,

        LocalDate startDate,

        LocalDate endDate
) {}
