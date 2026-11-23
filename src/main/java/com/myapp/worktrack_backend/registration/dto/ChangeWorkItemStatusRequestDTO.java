package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeWorkItemStatusRequestDTO(
        @NotBlank(message = "New status is required")
        String newStatus
) {}
