package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.*;
import com.myapp.worktrack_backend.registration.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * Service for Project CRUD and lifecycle management.
 *
 * Authorization rules:
 *   CREATE:  Super-Admin (any workspace) OR Admin assigned to target workspace+dept
 *   READ:    Super-Admin (all), Admin (own dept), Staff (membership gated)
 *   UPDATE:  Super-Admin OR Admin in same dept scope
 *   STATUS:  Super-Admin OR Admin in same dept scope (transitions validated)
 *
 * Valid status transitions:
 *   DRAFT    → ACTIVE, CANCELLED
 *   ACTIVE   → COMPLETED, CANCELLED
 *   COMPLETED / CANCELLED → (terminal — no further transitions)
 */
@Service
public class ProjectService {

    // Valid status transitions: key = current status, value = allowed next statuses
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "DRAFT",     Set.of("ACTIVE", "CANCELLED"),
            "ACTIVE",    Set.of("COMPLETED", "CANCELLED"),
            "COMPLETED", Set.of(),
            "CANCELLED", Set.of()
    );

    private final ProjectRepo projectRepo;
    private final WorkspaceRepo workspaceRepo;
    private final DepartmentRepo departmentRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;
    private final AuthenticatedEmployeeDetails authEmpDetails;
    private final ProjectMemberRepo projectMemberRepo;

    public ProjectService(ProjectRepo projectRepo,
                          WorkspaceRepo workspaceRepo,
                          DepartmentRepo departmentRepo,
                          EmployeeWorkspaceRepo employeeWorkspaceRepo,
                          AuthenticatedEmployeeDetails authEmpDetails,
                          ProjectMemberRepo projectMemberRepo) {
        this.projectRepo = projectRepo;
        this.workspaceRepo = workspaceRepo;
        this.departmentRepo = departmentRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
        this.authEmpDetails = authEmpDetails;
        this.projectMemberRepo = projectMemberRepo;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponseDTO createProject(CreateProjectRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();

        // Resolve and validate workspace
        Workspace workspace = workspaceRepo.findById(request.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFound("Workspace not found: " + request.workspaceId()));
        if (!"Active".equalsIgnoreCase(workspace.getStatus())) {
            throw new WorkspaceIsInactiveException("Workspace is inactive");
        }

        // Resolve and validate department
        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(request.workspaceId());
        dw.setDeptId(request.deptId());
        Department department = departmentRepo.findById(dw)
                .orElseThrow(() -> new DepartmentNotFoundException(
                        "Department not found: " + request.deptId() + " in workspace " + request.workspaceId()));
        if (!"Active".equalsIgnoreCase(department.getStatus())) {
            throw new DepartmentIsInactiveException("Department is inactive");
        }

        // Authorization: Super-Admin unrestricted; Admin must be in same workspace+dept
        authorizeProjectWrite(actor, request.workspaceId(), request.deptId());

        // Unique project key within (workspace, dept)
        if (projectRepo.existsByProjectKeyAndWorkspaceWIdAndDeptId(
                request.projectKey(), request.workspaceId(), request.deptId())) {
            throw new DuplicateProjectKeyException(
                    "Project key '" + request.projectKey() + "' already exists in this department");
        }

        Project project = new Project();
        project.setProjectKey(request.projectKey());
        project.setName(request.name());
        project.setDescription(request.description());
        project.setWorkspace(workspace);
        project.setDeptId(request.deptId());
        project.setCreatedByEmpId(actor.getEmpId());
        project.setStatus("DRAFT");
        project.setStartDate(request.startDate());
        project.setTargetEndDate(request.targetEndDate());

        Project saved = projectRepo.save(project);

        // Auto-assign creator as initial OWNER in project_members
        if (projectMemberRepo != null) {
            ProjectMember owner = new ProjectMember();
            owner.setProject(saved);
            owner.setEmployee(actor);
            owner.setRole(ProjectMemberRole.OWNER);
            owner.setStatus("ACTIVE");
            owner.setAddedByEmpId(actor.getEmpId());
            projectMemberRepo.save(owner);
        }

        return toResponseDTO(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProject(Long projectId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        authorizeProjectRead(actor, project);
        return toResponseDTO(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getProjectsByDepartment(int workspaceId, String deptId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();

        if ("Super-Admin".equals(actor.getRole())) {
            // Super-Admin can query any dept
        } else if ("Admin".equals(actor.getRole())) {
            // Admin must be assigned to this workspace+dept
            if (employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                    actor.getEmpId(), workspaceId, deptId) == 0) {
                throw new AccessDeniedException("Insufficient scope to view projects in this department");
            }
        } else {
            // Staff members can only see projects they belong to
            throw new AccessDeniedException("Staff members cannot list all department projects. Access via project membership.");
        }

        return projectRepo.findByWorkspaceWIdAndDeptId(workspaceId, deptId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getAllProjectsInWorkspace(int workspaceId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        if (!"Super-Admin".equals(actor.getRole())) {
            throw new AccessDeniedException("Only Super-Admin can list all projects across a workspace");
        }
        // Validate workspace exists
        workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFound("Workspace not found: " + workspaceId));
        return projectRepo.findByWorkspaceWId(workspaceId, pageable).map(this::toResponseDTO);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponseDTO updateProject(Long projectId, UpdateProjectRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        authorizeProjectWrite(actor, project.getWorkspace().getWId(), project.getDeptId());

        // Apply only non-null updates (partial update pattern)
        if (request.name() != null)                   project.setName(request.name());
        if (request.description() != null)            project.setDescription(request.description());
        if (request.startDate() != null)              project.setStartDate(request.startDate());
        if (request.targetEndDate() != null)          project.setTargetEndDate(request.targetEndDate());
        if (request.actualCompletionDate() != null)   project.setActualCompletionDate(request.actualCompletionDate());

        return toResponseDTO(projectRepo.save(project));
    }

    @Transactional
    public ProjectResponseDTO updateProjectStatus(Long projectId, String newStatus) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        authorizeProjectWrite(actor, project.getWorkspace().getWId(), project.getDeptId());

        // Validate transition
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(project.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidProjectStatusTransitionException(
                    "Cannot transition project from '" + project.getStatus() + "' to '" + newStatus + "'");
        }

        project.setStatus(newStatus);
        return toResponseDTO(projectRepo.save(project));
    }

    // ── Authorization helpers ─────────────────────────────────────────────────

    /**
     * Authorizes write operations (create, update, status change).
     * Super-Admin: unrestricted.
     * Admin: must be assigned to the target workspace+dept.
     * Staff: denied.
     */
    private void authorizeProjectWrite(Employee actor, int workspaceId, String deptId) {
        if ("Super-Admin".equals(actor.getRole())) {
            return; // unrestricted
        }
        if ("Admin".equals(actor.getRole())) {
            if (employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                    actor.getEmpId(), workspaceId, deptId) == 0) {
                throw new AccessDeniedException("Admin does not have scope over workspace "
                        + workspaceId + " / department " + deptId);
            }
            return;
        }
        throw new AccessDeniedException("Insufficient role to manage projects");
    }

    /**
     * Authorizes read access to a specific project.
     * Super-Admin: all projects.
     * Admin: projects in their workspace+dept scope.
     * Staff: active membership required.
     */
    private void authorizeProjectRead(Employee actor, Project project) {
        if ("Super-Admin".equals(actor.getRole())) return;
        if ("Admin".equals(actor.getRole())) {
            if (employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                    actor.getEmpId(), project.getWorkspace().getWId(), project.getDeptId()) == 0) {
                throw new AccessDeniedException("Insufficient scope to view this project");
            }
            return;
        }
        // Staff — check active membership
        if (projectMemberRepo != null && projectMemberRepo.existsByProjectProjectIdAndEmployeeEmployeeIdAndStatus(
                project.getProjectId(), actor.getEmployeeId(), "ACTIVE")) {
            return;
        }
        throw new AccessDeniedException("Access denied. Project membership required.");
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ProjectResponseDTO toResponseDTO(Project p) {
        return new ProjectResponseDTO(
                p.getProjectId(),
                p.getProjectKey(),
                p.getName(),
                p.getDescription(),
                p.getWorkspace().getWId(),
                p.getWorkspace().getWorkspaceName(),
                p.getDeptId(),
                p.getCreatedByEmpId(),
                p.getStatus(),
                p.getStartDate(),
                p.getTargetEndDate(),
                p.getActualCompletionDate(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
