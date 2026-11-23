package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.WorkItemComment;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        Long workItemId,
        Long authorEmployeeId,
        String authorEmpId,
        String content,
        boolean isEdited,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponseDTO fromEntity(WorkItemComment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getWorkItem().getId(),
                comment.getAuthor().getEmployeeId(),
                comment.getAuthor().getEmpId(),
                comment.isDeleted() ? "[This comment has been deleted]" : comment.getContent(),
                comment.isEdited(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
