package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddWorkItemsToSprintRequestDTO(
        @NotEmpty(message = "Work item IDs list must not be empty")
        List<Long> workItemIds
) {}
