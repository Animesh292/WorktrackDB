package com.myapp.worktrack_backend.registration.dto;

import java.util.Map;

public record StaffDashboardDTO(
        String empId,
        long myAccessibleProjectsCount,
        long myAssignedItemsCount,
        Map<String, Long> myItemsByStatus,
        long myReportedItemsCount,
        long myActiveSprintItemsCount,
        long myBlockedItemsCount,
        long myOverdueItemsCount
) {}
