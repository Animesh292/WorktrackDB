package com.myapp.worktrack_backend.registration.dto;

import java.util.Map;

public record DepartmentDashboardDTO(
        int workspaceId,
        String deptId,
        String deptName,
        String departmentHeadEmpId,
        long totalEmployeesInDept,
        long totalProjects,
        Map<String, Long> projectsByStatus,
        Map<String, Long> workItemsByStatus,
        Map<String, Long> workloadByAssignee,
        long activeSprintsCount,
        long blockedItemsCount,
        long overdueItemsCount
) {}
