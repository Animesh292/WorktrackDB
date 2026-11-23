package com.myapp.worktrack_backend.registration.dto;

import com.myapp.worktrack_backend.registration.entity.ProjectMember;

import java.time.LocalDateTime;

/**
 * Response DTO representing project membership details.
 */
public record ProjectMemberResponseDTO(
        Long id,
        Long projectId,
        String projectKey,
        Long employeeId,
        String empId,
        String role,
        String status,
        String addedByEmpId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectMemberResponseDTO fromEntity(ProjectMember member) {
        return new ProjectMemberResponseDTO(
                member.getId(),
                member.getProject().getProjectId(),
                member.getProject().getProjectKey(),
                member.getEmployee().getEmployeeId(),
                member.getEmployee().getEmpId(),
                member.getRole() != null ? member.getRole().name() : null,
                member.getStatus(),
                member.getAddedByEmpId(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
