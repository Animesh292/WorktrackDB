package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.Sprint;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintResponseDTO(
        Long id,
        Long projectId,
        String projectKey,
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        String state,
        String createdByEmpId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SprintResponseDTO fromEntity(Sprint sprint) {
        return new SprintResponseDTO(
                sprint.getId(),
                sprint.getProject().getProjectId(),
                sprint.getProject().getProjectKey(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getState() != null ? sprint.getState().name() : null,
                sprint.getCreatedByEmpId(),
                sprint.getCreatedAt(),
                sprint.getUpdatedAt()
        );
    }
}
