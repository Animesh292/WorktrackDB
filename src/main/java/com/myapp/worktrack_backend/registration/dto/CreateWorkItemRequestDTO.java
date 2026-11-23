package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateWorkItemRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        @NotBlank(message = "Type is required")
        String type,

        String priority,

        @PositiveOrZero(message = "Estimation points must be zero or positive")
        Integer estimationPoints,

        Long parentId,

        String assigneeEmpId,

        Long sprintId,

        String acceptanceCriteria,

        LocalDate startDate,

        LocalDate dueDate
) {}
