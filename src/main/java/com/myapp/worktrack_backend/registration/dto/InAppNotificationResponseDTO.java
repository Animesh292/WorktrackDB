package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.InAppNotification;

import java.time.LocalDateTime;

public record InAppNotificationResponseDTO(
        Long id,
        Long recipientEmployeeId,
        String recipientEmpId,
        String title,
        String message,
        String link,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static InAppNotificationResponseDTO fromEntity(InAppNotification n) {
        return new InAppNotificationResponseDTO(
                n.getId(),
                n.getRecipient().getEmployeeId(),
                n.getRecipient().getEmpId(),
                n.getTitle(),
                n.getMessage(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
