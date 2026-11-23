package com.myapp.worktrack_backend.registration.dto;

import java.util.Map;

public record ProjectMetricsDTO(
        Long projectId,
        String projectKey,
        String projectName,
        String projectStatus,
        long totalWorkItems,
        Map<String, Long> itemsByStatus,
        Map<String, Long> itemsByType,
        long totalEstimationPoints,
        long completedEstimationPoints,
        double completionPercentage,
        Long activeSprintId,
        String activeSprintName
) {}
