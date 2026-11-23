package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.DepartmentNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.repository.projection.AssigneeCountProjection;
import com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection;
import com.myapp.worktrack_backend.registration.repository.projection.TypeCountProjection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Scoped Executive and Personal Dashboards & Analytics (Section 16).
 * Uses DB-level aggregations and projections (no table scans in application memory).
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<WorkItemStatus> FINISHED_STATUSES = List.of(
            WorkItemStatus.COMPLETED,
            WorkItemStatus.CANCELLED
    );

    private final ProjectRepo projectRepo;
    private final WorkItemRepo workItemRepo;
    private final SprintRepo sprintRepo;
    private final EmployeeRepo employeeRepo;
    private final WorkspaceRepo workspaceRepo;
    private final DepartmentRepo departmentRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public DashboardService(ProjectRepo projectRepo,
                            WorkItemRepo workItemRepo,
                            SprintRepo sprintRepo,
                            EmployeeRepo employeeRepo,
                            WorkspaceRepo workspaceRepo,
                            DepartmentRepo departmentRepo,
                            EmployeeWorkspaceRepo employeeWorkspaceRepo,
                            ProjectMemberRepo projectMemberRepo,
                            ProjectPermissionService projectPermissionService,
                            AuthenticatedEmployeeDetails authEmpDetails) {
        this.projectRepo = projectRepo;
        this.workItemRepo = workItemRepo;
        this.sprintRepo = sprintRepo;
        this.employeeRepo = employeeRepo;
        this.workspaceRepo = workspaceRepo;
        this.departmentRepo = departmentRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
        this.projectMemberRepo = projectMemberRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
    }

    public SuperAdminDashboardDTO getSuperAdminDashboard() {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        if (!"Super-Admin".equalsIgnoreCase(actor.getRole())) {
            throw new AccessDeniedException("Only Super-Admin can access the global executive dashboard");
        }

        long totalWorkspaces = workspaceRepo.count();
        long totalDepartments = departmentRepo.count();
        long totalEmployees = employeeRepo.count();
        Map<String, Long> employeesByStatus = toMap(employeeRepo.countAllGroupedByEmployeeStatus());

        long totalProjects = projectRepo.count();
        Map<String, Long> projectsByStatus = toMap(projectRepo.countAllGroupedByStatus());

        long activeSprints = sprintRepo.countByState(SprintState.ACTIVE);
        long plannedSprints = sprintRepo.countByState(SprintState.PLANNED);
        long blockedItems = workItemRepo.countByStatus(WorkItemStatus.BLOCKED);
        long overdueItems = workItemRepo.countByDueDateBeforeAndStatusNotIn(LocalDate.now(), FINISHED_STATUSES);

        return new SuperAdminDashboardDTO(
                totalWorkspaces,
                totalDepartments,
                totalEmployees,
                employeesByStatus,
                totalProjects,
                projectsByStatus,
                activeSprints,
                plannedSprints,
                blockedItems,
                overdueItems
        );
    }

    public DepartmentDashboardDTO getDepartmentDashboard(int workspaceId, String deptId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();

        if ("Super-Admin".equalsIgnoreCase(actor.getRole())) {
            // Super-Admin has global bypass
        } else if ("Admin".equalsIgnoreCase(actor.getRole())) {
            boolean inScope = employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(actor.getEmpId(), workspaceId, deptId) > 0;
            if (!inScope) {
                throw new AccessDeniedException("Admin is not assigned to workspace " + workspaceId + " and department " + deptId);
            }
        } else {
            throw new AccessDeniedException("Staff members cannot access departmental executive dashboards");
        }

        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(workspaceId);
        dw.setDeptId(deptId);
        Department dept = departmentRepo.findById(dw)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + deptId + " in workspace " + workspaceId));

        String hodEmpId = dept.getDepartmentHead() != null ? dept.getDepartmentHead().getEmpId() : null;
        long totalEmployeesInDept = employeeWorkspaceRepo.countWorkDeptEmployees(deptId, workspaceId);
        long totalProjects = projectRepo.countByWorkspaceWIdAndDeptId(workspaceId, deptId);

        Map<String, Long> projectsByStatus = toMap(projectRepo.countByDeptGroupedByStatus(workspaceId, deptId));
        Map<String, Long> workItemsByStatus = toMap(workItemRepo.countByDepartmentGroupedByStatus(workspaceId, deptId));

        Map<String, Long> workload = new HashMap<>();
        for (AssigneeCountProjection p : workItemRepo.countWorkloadByDepartment(workspaceId, deptId)) {
            if (p.getAssigneeEmpId() != null) {
                workload.put(p.getAssigneeEmpId(), p.getCount());
            }
        }

        long activeSprints = sprintRepo.countByProjectWorkspaceWIdAndProjectDeptIdAndState(workspaceId, deptId, SprintState.ACTIVE);
        long blockedItems = workItemRepo.countByProjectWorkspaceWIdAndProjectDeptIdAndStatus(workspaceId, deptId, WorkItemStatus.BLOCKED);
        long overdueItems = workItemRepo.countByProjectWorkspaceWIdAndProjectDeptIdAndDueDateBeforeAndStatusNotIn(
                workspaceId, deptId, LocalDate.now(), FINISHED_STATUSES);

        return new DepartmentDashboardDTO(
                workspaceId,
                deptId,
                dept.getDeptName(),
                hodEmpId,
                totalEmployeesInDept,
                totalProjects,
                projectsByStatus,
                workItemsByStatus,
                workload,
                activeSprints,
                blockedItems,
                overdueItems
        );
    }

    public StaffDashboardDTO getStaffDashboard() {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();

        long myProjects = projectMemberRepo.countByEmployeeEmployeeIdAndStatus(actor.getEmployeeId(), "ACTIVE");
        Map<String, Long> myItemsByStatus = toMap(workItemRepo.countByAssigneeGroupedByStatus(actor.getEmpId()));

        long myAssignedItems = myItemsByStatus.values().stream().mapToLong(Long::longValue).sum();
        long myReportedItems = workItemRepo.countByReporterEmpId(actor.getEmpId());
        long myBlockedItems = workItemRepo.countByAssigneeEmpIdAndStatus(actor.getEmpId(), WorkItemStatus.BLOCKED);
        long myOverdueItems = workItemRepo.countByAssigneeEmpIdAndDueDateBeforeAndStatusNotIn(
                actor.getEmpId(), LocalDate.now(), FINISHED_STATUSES);

        // Current sprint items assigned to me
        long myActiveSprintItems = 0; // count of items assigned in active sprint
        Long activeSprintWorkload = myItemsByStatus.getOrDefault(WorkItemStatus.IN_PROGRESS.name(), 0L)
                + myItemsByStatus.getOrDefault(WorkItemStatus.READY.name(), 0L);

        return new StaffDashboardDTO(
                actor.getEmpId(),
                myProjects,
                myAssignedItems,
                myItemsByStatus,
                myReportedItems,
                activeSprintWorkload,
                myBlockedItems,
                myOverdueItems
        );
    }

    public ProjectMetricsDTO getProjectMetrics(Long projectId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        Map<String, Long> itemsByStatus = toMap(workItemRepo.countByProjectGroupedByStatus(projectId));
        Map<String, Long> itemsByType = toTypeMap(workItemRepo.countByProjectGroupedByType(projectId));

        long totalItems = itemsByStatus.values().stream().mapToLong(Long::longValue).sum();
        long totalPoints = workItemRepo.sumEstimationPointsByProjectId(projectId);
        long completedPoints = workItemRepo.sumCompletedEstimationPointsByProjectId(projectId);
        double completionRate = totalPoints > 0 ? ((double) completedPoints / totalPoints) * 100.0 : 0.0;

        Optional<Sprint> activeSprintOpt = sprintRepo.findByProjectProjectIdAndState(projectId, SprintState.ACTIVE);
        Long activeSprintId = activeSprintOpt.map(Sprint::getId).orElse(null);
        String activeSprintName = activeSprintOpt.map(Sprint::getName).orElse(null);

        return new ProjectMetricsDTO(
                projectId,
                project.getProjectKey(),
                project.getName(),
                project.getStatus(),
                totalItems,
                itemsByStatus,
                itemsByType,
                totalPoints,
                completedPoints,
                Math.round(completionRate * 10.0) / 10.0,
                activeSprintId,
                activeSprintName
        );
    }

    private Map<String, Long> toMap(List<StatusCountProjection> projections) {
        Map<String, Long> map = new HashMap<>();
        if (projections != null) {
            for (StatusCountProjection p : projections) {
                if (p.getStatus() != null) {
                    map.put(p.getStatus().toString(), p.getCount());
                }
            }
        }
        return map;
    }

    private Map<String, Long> toTypeMap(List<TypeCountProjection> projections) {
        Map<String, Long> map = new HashMap<>();
        if (projections != null) {
            for (TypeCountProjection p : projections) {
                if (p.getType() != null) {
                    map.put(p.getType().toString(), p.getCount());
                }
            }
        }
        return map;
    }
}
