package com.myapp.worktrack_backend.registration.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO returned for project queries.
 * Never exposes the JPA entity directly.
 */
public record ProjectResponseDTO(
        Long projectId,
        String projectKey,
        String name,
        String description,
        int workspaceId,
        String workspaceName,
        String deptId,
        String createdByEmpId,
        String status,
        LocalDate startDate,
        LocalDate targetEndDate,
        LocalDate actualCompletionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
