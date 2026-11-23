package com.myapp.worktrack_backend.registration.dto;

import java.util.Map;

public record SuperAdminDashboardDTO(
        long totalWorkspaces,
        long totalDepartments,
        long totalEmployees,
        Map<String, Long> employeesByStatus,
        long totalProjects,
        Map<String, Long> projectsByStatus,
        long activeSprintsCount,
        long plannedSprintsCount,
        long blockedWorkItemsCount,
        long overdueWorkItemsCount
) {}
