package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectMemberRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Centralized evaluator for project-level permissions.
 *
 * Rules:
 * - Inactive employees have no permissions.
 * - Super-Admin has global bypass for all project permissions.
 * - Admin assigned to the project's workspace + department has managerial authority.
 * - Staff (and other employees) must have an active ProjectMember record granting the requested permission.
 */
@Service
public class ProjectPermissionService {

    private static final Map<ProjectMemberRole, Set<ProjectPermission>> ROLE_PERMISSIONS = Map.of(
            ProjectMemberRole.OWNER, EnumSet.allOf(ProjectPermission.class),

            ProjectMemberRole.MANAGER, EnumSet.of(
                    ProjectPermission.VIEW_PROJECT,
                    ProjectPermission.UPDATE_PROJECT_METADATA,
                    ProjectPermission.MANAGE_MEMBERS,
                    ProjectPermission.CREATE_WORK_ITEMS,
                    ProjectPermission.UPDATE_OWN_WORK_ITEMS,
                    ProjectPermission.UPDATE_ANY_WORK_ITEMS,
                    ProjectPermission.ASSIGN_WORK_ITEMS,
                    ProjectPermission.CHANGE_WORK_ITEM_STATUS,
                    ProjectPermission.MANAGE_BACKLOG,
                    ProjectPermission.MANAGE_SPRINTS,
                    ProjectPermission.REVIEW_WORK,
                    ProjectPermission.VIEW_REPORTS,
                    ProjectPermission.COMMENT,
                    ProjectPermission.MANAGE_ATTACHMENTS
            ),

            ProjectMemberRole.CONTRIBUTOR, EnumSet.of(
                    ProjectPermission.VIEW_PROJECT,
                    ProjectPermission.CREATE_WORK_ITEMS,
                    ProjectPermission.UPDATE_OWN_WORK_ITEMS,
                    ProjectPermission.CHANGE_WORK_ITEM_STATUS,
                    ProjectPermission.COMMENT,
                    ProjectPermission.VIEW_REPORTS
            ),

            ProjectMemberRole.VIEWER, EnumSet.of(
                    ProjectPermission.VIEW_PROJECT,
                    ProjectPermission.COMMENT,
                    ProjectPermission.VIEW_REPORTS
            )
    );

    private final ProjectMemberRepo projectMemberRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;

    public ProjectPermissionService(ProjectMemberRepo projectMemberRepo,
                                  EmployeeWorkspaceRepo employeeWorkspaceRepo) {
        this.projectMemberRepo = projectMemberRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
    }

    public boolean hasPermission(Employee actor, Project project, ProjectPermission permission) {
        if (actor == null || !"Active".equalsIgnoreCase(actor.getEmployeeStatus())) {
            return false;
        }

        // 1. Super-Admin has global access
        if ("Super-Admin".equalsIgnoreCase(actor.getRole())) {
            return true;
        }

        // 2. Department Admin oversight
        if ("Admin".equalsIgnoreCase(actor.getRole())) {
            boolean inScope = employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                    actor.getEmpId(),
                    project.getWorkspace().getWId(),
                    project.getDeptId()
            ) > 0;
            if (inScope) {
                return true;
            }
        }

        // 3. Project Member role check
        Optional<ProjectMember> membership = projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(
                project.getProjectId(),
                actor.getEmployeeId(),
                "ACTIVE"
        );

        if (membership.isEmpty()) {
            return false;
        }

        Set<ProjectPermission> permissions = ROLE_PERMISSIONS.getOrDefault(membership.get().getRole(), Set.of());
        return permissions.contains(permission);
    }

    public void assertPermission(Employee actor, Project project, ProjectPermission permission) {
        if (!hasPermission(actor, project, permission)) {
            throw new AccessDeniedException(String.format(
                    "Actor '%s' does not have permission '%s' on project '%s' (ID: %d)",
                    actor.getEmpId(), permission.name(), project.getName(), project.getProjectId()
            ));
        }
    }

    public Optional<ProjectMemberRole> getProjectRole(Employee actor, Project project) {
        if (actor == null || project == null) {
            return Optional.empty();
        }
        return projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(
                project.getProjectId(),
                actor.getEmployeeId(),
                "ACTIVE"
        ).map(ProjectMember::getRole);
    }
}
