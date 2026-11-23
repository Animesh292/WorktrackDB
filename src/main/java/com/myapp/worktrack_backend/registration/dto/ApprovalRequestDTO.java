package com.myapp.worktrack_backend.registration.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
public class ApprovalRequestDTO {
    @NotBlank private String role;
    @NotBlank private String departmentId;
    @Positive private int workspaceId;
}
