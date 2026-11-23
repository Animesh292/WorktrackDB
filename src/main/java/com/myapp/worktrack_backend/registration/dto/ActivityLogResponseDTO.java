package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.ActivityLog;

import java.time.LocalDateTime;

public record ActivityLogResponseDTO(
        Long id,
        Long projectId,
        String entityType,
        Long entityId,
        String action,
        String actorEmpId,
        String description,
        String oldValue,
        String newValue,
        LocalDateTime timestamp
) {
    public static ActivityLogResponseDTO fromEntity(ActivityLog log) {
        return new ActivityLogResponseDTO(
                log.getId(),
                log.getProjectId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getActorEmpId(),
                log.getDescription(),
                log.getOldValue(),
                log.getNewValue(),
                log.getTimestamp()
        );
    }
}
