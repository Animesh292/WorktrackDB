package com.myapp.worktrack_backend.project;

import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectMemberRepo;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionServiceTest {

    @Mock private ProjectMemberRepo projectMemberRepo;
    @Mock private EmployeeWorkspaceRepo employeeWorkspaceRepo;

    @InjectMocks
    private ProjectPermissionService permissionService;

    private Workspace workspace;
    private Project project;
    private Employee superAdmin;
    private Employee scopedAdmin;
    private Employee staff;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        workspace.setWId(1);

        project = new Project();
        project.setProjectId(50L);
        project.setName("Project Alpha");
        project.setWorkspace(workspace);
        project.setDeptId("D001");

        superAdmin = new Employee();
        superAdmin.setEmployeeId(1L);
        superAdmin.setEmpId("SA01");
        superAdmin.setRole("Super-Admin");
        superAdmin.setEmployeeStatus("Active");

        scopedAdmin = new Employee();
        scopedAdmin.setEmployeeId(2L);
        scopedAdmin.setEmpId("AD01");
        scopedAdmin.setRole("Admin");
        scopedAdmin.setEmployeeStatus("Active");

        staff = new Employee();
        staff.setEmployeeId(3L);
        staff.setEmpId("ST01");
        staff.setRole("Staff");
        staff.setEmployeeStatus("Active");
    }

    @Test
    void superAdminHasAllPermissions() {
        assertTrue(permissionService.hasPermission(superAdmin, project, ProjectPermission.VIEW_PROJECT));
        assertTrue(permissionService.hasPermission(superAdmin, project, ProjectPermission.MANAGE_MEMBERS));
        assertTrue(permissionService.hasPermission(superAdmin, project, ProjectPermission.UPDATE_PROJECT_METADATA));
    }

    @Test
    void scopedAdminHasPermissionsInDept() {
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("AD01", 1, "D001")).thenReturn(1L);

        assertTrue(permissionService.hasPermission(scopedAdmin, project, ProjectPermission.VIEW_PROJECT));
        assertTrue(permissionService.hasPermission(scopedAdmin, project, ProjectPermission.MANAGE_MEMBERS));
    }

    @Test
    void outOfScopeAdminWithoutMembershipIsDenied() {
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("AD01", 1, "D001")).thenReturn(0L);
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(50L, 2L, "ACTIVE"))
                .thenReturn(Optional.empty());

        assertFalse(permissionService.hasPermission(scopedAdmin, project, ProjectPermission.VIEW_PROJECT));
        assertThrows(AccessDeniedException.class, () ->
                permissionService.assertPermission(scopedAdmin, project, ProjectPermission.VIEW_PROJECT));
    }

    @Test
    void staffWithContributorRole_hasCorrectPermissions() {
        ProjectMember member = new ProjectMember();
        member.setRole(ProjectMemberRole.CONTRIBUTOR);
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(50L, 3L, "ACTIVE"))
                .thenReturn(Optional.of(member));

        assertTrue(permissionService.hasPermission(staff, project, ProjectPermission.VIEW_PROJECT));
        assertTrue(permissionService.hasPermission(staff, project, ProjectPermission.CREATE_WORK_ITEMS));
        assertFalse(permissionService.hasPermission(staff, project, ProjectPermission.MANAGE_MEMBERS));
    }

    @Test
    void staffWithViewerRole_cannotCreateWorkItems() {
        ProjectMember member = new ProjectMember();
        member.setRole(ProjectMemberRole.VIEWER);
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(50L, 3L, "ACTIVE"))
                .thenReturn(Optional.of(member));

        assertTrue(permissionService.hasPermission(staff, project, ProjectPermission.VIEW_PROJECT));
        assertFalse(permissionService.hasPermission(staff, project, ProjectPermission.CREATE_WORK_ITEMS));
    }

    @Test
    void inactiveEmployeeHasNoPermissions() {
        staff.setEmployeeStatus("Inactive");
        assertFalse(permissionService.hasPermission(staff, project, ProjectPermission.VIEW_PROJECT));
    }
}
