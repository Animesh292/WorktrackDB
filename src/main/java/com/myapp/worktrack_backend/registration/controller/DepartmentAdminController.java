package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.AssignDeptHeadRequestDTO;
import com.myapp.worktrack_backend.registration.dto.DepartmentResponseDTO;
import com.myapp.worktrack_backend.registration.service.DepartmentAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-level department management endpoints.
 *
 * All endpoints require a valid JWT (authenticated).
 * Service layer enforces Super-Admin role for HOD assignment.
 *
 * PUT  /admin/workspaces/{workspaceId}/departments/{deptId}/head  — assign HOD
 * DELETE /admin/workspaces/{workspaceId}/departments/{deptId}/head — clear HOD
 */
@RestController
@RequestMapping("/admin/workspaces/{workspaceId}/departments/{deptId}")
public class DepartmentAdminController {

    private final DepartmentAdminService departmentAdminService;

    public DepartmentAdminController(DepartmentAdminService departmentAdminService) {
        this.departmentAdminService = departmentAdminService;
    }

    /**
     * Assign an eligible employee as Department Head (HOD).
     * Actor must be an active Super-Admin.
     * HOD candidate must be: Active, Admin role, in same workspace.
     */
    @PutMapping("/head")
    public ResponseEntity<DepartmentResponseDTO> assignDepartmentHead(
            @PathVariable int workspaceId,
            @PathVariable String deptId,
            @Valid @RequestBody AssignDeptHeadRequestDTO request) {
        DepartmentResponseDTO response = departmentAdminService
                .assignDepartmentHead(workspaceId, deptId, request.empId());
        return ResponseEntity.ok(response);
    }

    /**
     * Clear the Department Head from a department.
     * Actor must be an active Super-Admin.
     */
    @DeleteMapping("/head")
    public ResponseEntity<DepartmentResponseDTO> clearDepartmentHead(
            @PathVariable int workspaceId,
            @PathVariable String deptId) {
        DepartmentResponseDTO response = departmentAdminService
                .clearDepartmentHead(workspaceId, deptId);
        return ResponseEntity.ok(response);
    }
}
