package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateWorkItemRequestDTO(
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        String priority,

        @PositiveOrZero(message = "Estimation points must be zero or positive")
        Integer estimationPoints,

        Long parentId,

        String acceptanceCriteria,

        LocalDate startDate,

        LocalDate dueDate
) {}
