package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.WorkItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WorkItemResponseDTO(
        Long id,
        Long projectId,
        String projectKey,
        String itemKey,
        Integer itemNumber,
        String title,
        String description,
        String type,
        String status,
        String priority,
        Integer estimationPoints,
        Long parentId,
        String parentKey,
        String parentTitle,
        Long assigneeEmployeeId,
        String assigneeEmpId,
        String reporterEmpId,
        Long sprintId,
        String acceptanceCriteria,
        LocalDate startDate,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkItemResponseDTO fromEntity(WorkItem w) {
        return new WorkItemResponseDTO(
                w.getId(),
                w.getProject().getProjectId(),
                w.getProject().getProjectKey(),
                w.getItemKey(),
                w.getItemNumber(),
                w.getTitle(),
                w.getDescription(),
                w.getType() != null ? w.getType().name() : null,
                w.getStatus() != null ? w.getStatus().name() : null,
                w.getPriority() != null ? w.getPriority().name() : null,
                w.getEstimationPoints(),
                w.getParent() != null ? w.getParent().getId() : null,
                w.getParent() != null ? w.getParent().getItemKey() : null,
                w.getParent() != null ? w.getParent().getTitle() : null,
                w.getAssignee() != null ? w.getAssignee().getEmployeeId() : null,
                w.getAssignee() != null ? w.getAssignee().getEmpId() : null,
                w.getReporterEmpId(),
                w.getSprintId(),
                w.getAcceptanceCriteria(),
                w.getStartDate(),
                w.getDueDate(),
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }
}
