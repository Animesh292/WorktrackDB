package com.myapp.worktrack_backend.dashboard;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.DepartmentDashboardDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectMetricsDTO;
import com.myapp.worktrack_backend.registration.dto.StaffDashboardDTO;
import com.myapp.worktrack_backend.registration.dto.SuperAdminDashboardDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.repository.projection.AssigneeCountProjection;
import com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection;
import com.myapp.worktrack_backend.registration.service.DashboardService;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProjectRepo projectRepo;
    @Mock private WorkItemRepo workItemRepo;
    @Mock private SprintRepo sprintRepo;
    @Mock private EmployeeRepo employeeRepo;
    @Mock private WorkspaceRepo workspaceRepo;
    @Mock private DepartmentRepo departmentRepo;
    @Mock private EmployeeWorkspaceRepo employeeWorkspaceRepo;
    @Mock private ProjectMemberRepo projectMemberRepo;
    @Mock private ProjectPermissionService projectPermissionService;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;

    @InjectMocks
    private DashboardService dashboardService;

    private Employee superAdmin;
    private Employee admin;
    private Employee staff;
    private Project project;
    private Department department;

    @BeforeEach
    void setUp() {
        superAdmin = new Employee();
        superAdmin.setEmployeeId(1L);
        superAdmin.setEmpId("SA001");
        superAdmin.setRole("Super-Admin");

        admin = new Employee();
        admin.setEmployeeId(2L);
        admin.setEmpId("AD001");
        admin.setRole("Admin");

        staff = new Employee();
        staff.setEmployeeId(3L);
        staff.setEmpId("ST001");
        staff.setRole("Staff");

        project = new Project();
        project.setProjectId(10L);
        project.setProjectKey("PROJ");
        project.setName("Project Alpha");
        project.setStatus("ACTIVE");

        department = new Department();
        department.setDeptName("Engineering");
    }

    @Test
    void getSuperAdminDashboard_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdmin);
        when(workspaceRepo.count()).thenReturn(3L);
        when(departmentRepo.count()).thenReturn(10L);
        when(employeeRepo.count()).thenReturn(50L);
        when(projectRepo.count()).thenReturn(8L);
        when(sprintRepo.countByState(SprintState.ACTIVE)).thenReturn(4L);
        when(sprintRepo.countByState(SprintState.PLANNED)).thenReturn(6L);

        SuperAdminDashboardDTO dashboard = dashboardService.getSuperAdminDashboard();

        assertNotNull(dashboard);
        assertEquals(3L, dashboard.totalWorkspaces());
        assertEquals(10L, dashboard.totalDepartments());
        assertEquals(50L, dashboard.totalEmployees());
        assertEquals(8L, dashboard.totalProjects());
        assertEquals(4L, dashboard.activeSprintsCount());
        assertEquals(6L, dashboard.plannedSprintsCount());
    }

    @Test
    void getSuperAdminDashboard_nonSuperAdmin_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(admin);
        assertThrows(AccessDeniedException.class, () -> dashboardService.getSuperAdminDashboard());
    }

    @Test
    void getDepartmentDashboard_adminInScope_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(admin);
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("AD001", 1, "D001")).thenReturn(1L);
        when(departmentRepo.findById(any())).thenReturn(Optional.of(department));
        when(employeeWorkspaceRepo.countWorkDeptEmployees("D001", 1)).thenReturn(15L);
        when(projectRepo.countByWorkspaceWIdAndDeptId(1, "D001")).thenReturn(5L);

        DepartmentDashboardDTO dashboard = dashboardService.getDepartmentDashboard(1, "D001");

        assertNotNull(dashboard);
        assertEquals("Engineering", dashboard.deptName());
        assertEquals(15L, dashboard.totalEmployeesInDept());
        assertEquals(5L, dashboard.totalProjects());
    }

    @Test
    void getDepartmentDashboard_adminOutOfScope_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(admin);
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("AD001", 1, "D001")).thenReturn(0L);

        assertThrows(AccessDeniedException.class, () -> dashboardService.getDepartmentDashboard(1, "D001"));
    }

    @Test
    void getDepartmentDashboard_staff_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staff);
        assertThrows(AccessDeniedException.class, () -> dashboardService.getDepartmentDashboard(1, "D001"));
    }

    @Test
    void getStaffDashboard_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staff);
        when(projectMemberRepo.countByEmployeeEmployeeIdAndStatus(3L, "ACTIVE")).thenReturn(2L);
        when(workItemRepo.countByReporterEmpId("ST001")).thenReturn(4L);
        when(workItemRepo.countByAssigneeEmpIdAndStatus("ST001", WorkItemStatus.BLOCKED)).thenReturn(1L);
        when(workItemRepo.countActiveSprintItemsForStaff("ST001", 3L)).thenReturn(5L);

        StaffDashboardDTO dashboard = dashboardService.getStaffDashboard();

        assertNotNull(dashboard);
        assertEquals("ST001", dashboard.empId());
        assertEquals(2L, dashboard.myAccessibleProjectsCount());
        assertEquals(4L, dashboard.myReportedItemsCount());
        assertEquals(1L, dashboard.myBlockedItemsCount());
        assertEquals(5L, dashboard.myActiveSprintItemsCount());
    }

    @Test
    void getProjectMetrics_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staff);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.sumEstimationPointsByProjectId(10L)).thenReturn(100L);
        when(workItemRepo.sumCompletedEstimationPointsByProjectId(10L)).thenReturn(75L);
        when(sprintRepo.findByProjectProjectIdAndState(10L, SprintState.ACTIVE)).thenReturn(Optional.empty());

        ProjectMetricsDTO metrics = dashboardService.getProjectMetrics(10L);

        assertNotNull(metrics);
        assertEquals("PROJ", metrics.projectKey());
        assertEquals(100L, metrics.totalEstimationPoints());
        assertEquals(75L, metrics.completedEstimationPoints());
        assertEquals(75.0, metrics.completionPercentage());
        assertNull(metrics.activeSprintId());
    }
}
