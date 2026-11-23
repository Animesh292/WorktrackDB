package com.myapp.worktrack_backend.registration.event;

/**
 * Domain Event capturing collaborative project actions.
 * Used for decoupled asynchronous audit logging and notification dispatching.
 */
public record ProjectActivityEvent(
        Long projectId,
        String entityType,
        Long entityId,
        String action,
        String actorEmpId,
        String description,
        String oldValue,
        String newValue,
        Long recipientEmployeeId,
        String notificationTitle,
        String notificationMessage,
        String notificationLink
) {
    public static ProjectActivityEvent forAuditOnly(
            Long projectId, String entityType, Long entityId,
            String action, String actorEmpId, String description,
            String oldValue, String newValue) {
        return new ProjectActivityEvent(
                projectId, entityType, entityId, action, actorEmpId, description,
                oldValue, newValue, null, null, null, null
        );
    }
}
