package com.myapp.worktrack_backend.registration.dto;

import java.math.BigDecimal;

/**
 * Response DTO for department details including HOD info.
 */
public record DepartmentResponseDTO(
        String deptId,
        int workspaceId,
        String deptName,
        String status,
        Integer capacity,
        BigDecimal budget,
        String deptHeadEmpId,
        String deptHeadName
) {}
