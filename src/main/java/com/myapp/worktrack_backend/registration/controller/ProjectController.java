package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Project lifecycle and management.
 *
 * All endpoints require a valid JWT (authenticated).
 * Service layer enforces role-based authorization.
 *
 * Endpoints:
 *   POST   /projects                                        — create project
 *   GET    /projects/{projectId}                            — get one project
 *   GET    /workspaces/{wId}/departments/{deptId}/projects  — list by dept (Admin/Super-Admin)
 *   GET    /workspaces/{wId}/projects                       — list by workspace (Super-Admin)
 *   PUT    /projects/{projectId}                            — update project metadata
 *   PATCH  /projects/{projectId}/status                     — update project status
 */
@RestController
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** Create a new project. Actor must be Admin in target dept scope or Super-Admin. */
    @PostMapping("/projects")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody CreateProjectRequestDTO request) {
        ProjectResponseDTO response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Get a single project by ID. Access is scoped by actor role. */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    /**
     * List all projects in a department (paginated).
     * Admin must be assigned to this workspace+dept. Super-Admin unrestricted. Staff denied.
     */
    @GetMapping("/workspaces/{workspaceId}/departments/{deptId}/projects")
    public ResponseEntity<Page<ProjectResponseDTO>> getProjectsByDepartment(
            @PathVariable int workspaceId,
            @PathVariable String deptId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                projectService.getProjectsByDepartment(workspaceId, deptId, pageable));
    }

    /**
     * List all projects across a workspace (paginated).
     * Super-Admin only.
     */
    @GetMapping("/workspaces/{workspaceId}/projects")
    public ResponseEntity<Page<ProjectResponseDTO>> getAllProjectsInWorkspace(
            @PathVariable int workspaceId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                projectService.getAllProjectsInWorkspace(workspaceId, pageable));
    }

    /**
     * Update project metadata (name, description, dates).
     * Admin in dept scope or Super-Admin.
     */
    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequestDTO request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    /**
     * Transition project status.
     * Valid transitions: DRAFT→ACTIVE, DRAFT→CANCELLED, ACTIVE→COMPLETED, ACTIVE→CANCELLED.
     */
    @PatchMapping("/projects/{projectId}/status")
    public ResponseEntity<ProjectResponseDTO> updateProjectStatus(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectStatusUpdateDTO request) {
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, request.newStatus()));
    }
}
