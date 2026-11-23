package com.myapp.worktrack_backend.project;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.CreateProjectRequestDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectResponseDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectStatusUpdateDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateProjectRequestDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.*;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepo projectRepo;
    @Mock private WorkspaceRepo workspaceRepo;
    @Mock private DepartmentRepo departmentRepo;
    @Mock private EmployeeWorkspaceRepo employeeWorkspaceRepo;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;
    @Mock private ProjectMemberRepo projectMemberRepo;

    @InjectMocks
    private ProjectService projectService;

    // ── Shared test fixtures ──────────────────────────────────────────────────

    private Employee adminActor;
    private Employee superAdminActor;
    private Employee staffActor;
    private Workspace workspace;
    private Department department;

    @BeforeEach
    void setUp() {
        adminActor = new Employee();
        adminActor.setEmployeeId(1L);
        adminActor.setEmpId("EM000001");
        adminActor.setRole("Admin");
        adminActor.setEmployeeStatus("Active");

        superAdminActor = new Employee();
        superAdminActor.setEmployeeId(2L);
        superAdminActor.setEmpId("EM000002");
        superAdminActor.setRole("Super-Admin");
        superAdminActor.setEmployeeStatus("Active");

        staffActor = new Employee();
        staffActor.setEmployeeId(3L);
        staffActor.setEmpId("EM000003");
        staffActor.setRole("Staff");
        staffActor.setEmployeeStatus("Active");

        workspace = new Workspace();
        workspace.setWId(1);
        workspace.setWorkspaceName("HQ");
        workspace.setStatus("Active");

        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setDeptId("D001");
        dw.setWId(1);
        department = new Department();
        department.setDepartmentwkspc(dw);
        department.setDeptName("Engineering");
        department.setStatus("Active");
        department.setWorkspace(workspace);
        department.setCapacity(50);
    }

    private CreateProjectRequestDTO buildCreateRequest() {
        return new CreateProjectRequestDTO(
                "WT-001", "WorkTrack Core", "Core project", 1, "D001",
                LocalDate.now(), LocalDate.now().plusMonths(3));
    }

    private Project buildSavedProject() {
        Project p = new Project();
        p.setProjectId(1L);
        p.setProjectKey("WT-001");
        p.setName("WorkTrack Core");
        p.setDescription("Core project");
        p.setWorkspace(workspace);
        p.setDeptId("D001");
        p.setCreatedByEmpId("EM000001");
        p.setStatus("DRAFT");
        return p;
    }

    // ── CREATE tests ──────────────────────────────────────────────────────────

    @Test
    void adminInScopeCanCreateProject() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(adminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000001", 1, "D001")).thenReturn(1L);
        when(projectRepo.existsByProjectKeyAndWorkspaceWIdAndDeptId("WT-001", 1, "D001")).thenReturn(false);
        when(projectRepo.save(any(Project.class))).thenReturn(buildSavedProject());

        ProjectResponseDTO result = projectService.createProject(buildCreateRequest());

        assertNotNull(result);
        assertEquals("WT-001", result.projectKey());
        assertEquals("DRAFT", result.status());
        verify(projectRepo).save(any(Project.class));
    }

    @Test
    void adminOutOfScopeCannotCreateProject() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(adminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000001", 1, "D001")).thenReturn(0L);

        assertThrows(AccessDeniedException.class,
                () -> projectService.createProject(buildCreateRequest()));
        verify(projectRepo, never()).save(any());
    }

    @Test
    void superAdminCanCreateProjectInAnyDept() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));
        when(projectRepo.existsByProjectKeyAndWorkspaceWIdAndDeptId("WT-001", 1, "D001")).thenReturn(false);
        when(projectRepo.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setProjectId(1L);
            return p;
        });

        ProjectResponseDTO result = projectService.createProject(buildCreateRequest());
        assertEquals("WT-001", result.projectKey());
        // No scope check called for Super-Admin
        verify(employeeWorkspaceRepo, never()).isAssignedToWorkspaceAndDepartment(any(), anyInt(), any());
    }

    @Test
    void staffCannotCreateProject() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staffActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));

        assertThrows(AccessDeniedException.class,
                () -> projectService.createProject(buildCreateRequest()));
    }

    @Test
    void duplicateProjectKeyIsRejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));
        when(projectRepo.existsByProjectKeyAndWorkspaceWIdAndDeptId("WT-001", 1, "D001")).thenReturn(true);

        assertThrows(DuplicateProjectKeyException.class,
                () -> projectService.createProject(buildCreateRequest()));
        verify(projectRepo, never()).save(any());
    }

    @Test
    void inactiveWorkspaceBlocksProjectCreation() {
        workspace.setStatus("Inactive");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));

        assertThrows(WorkspaceIsInactiveException.class,
                () -> projectService.createProject(buildCreateRequest()));
    }

    @Test
    void inactiveDepartmentBlocksProjectCreation() {
        department.setStatus("Inactive");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(departmentRepo.findById(any(DepartmentWorkspace.class))).thenReturn(Optional.of(department));

        assertThrows(DepartmentIsInactiveException.class,
                () -> projectService.createProject(buildCreateRequest()));
    }

    // ── STATUS TRANSITION tests ───────────────────────────────────────────────

    @Test
    void validStatusTransitionDraftToActive() {
        Project project = buildSavedProject(); // status = DRAFT
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponseDTO result = projectService.updateProjectStatus(1L, "ACTIVE");
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void validStatusTransitionActiveToCompleted() {
        Project project = buildSavedProject();
        project.setStatus("ACTIVE");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponseDTO result = projectService.updateProjectStatus(1L, "COMPLETED");
        assertEquals("COMPLETED", result.status());
    }

    @Test
    void invalidStatusTransitionCompletedToDraftIsRejected() {
        Project project = buildSavedProject();
        project.setStatus("COMPLETED");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> projectService.updateProjectStatus(1L, "DRAFT"));
    }

    @Test
    void invalidStatusTransitionCancelledToActiveIsRejected() {
        Project project = buildSavedProject();
        project.setStatus("CANCELLED");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> projectService.updateProjectStatus(1L, "ACTIVE"));
    }

    // ── READ tests ────────────────────────────────────────────────────────────

    @Test
    void staffCannotListAllDepartmentProjects() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staffActor);
        assertThrows(AccessDeniedException.class,
                () -> projectService.getProjectsByDepartment(1, "D001", Pageable.unpaged()));
    }

    @Test
    void adminCanListProjectsInOwnDept() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(adminActor);
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000001", 1, "D001")).thenReturn(1L);
        when(projectRepo.findByWorkspaceWIdAndDeptId(eq(1), eq("D001"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildSavedProject())));

        var result = projectService.getProjectsByDepartment(1, "D001", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void nonSuperAdminCannotListAllWorkspaceProjects() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(adminActor);
        assertThrows(AccessDeniedException.class,
                () -> projectService.getAllProjectsInWorkspace(1, Pageable.unpaged()));
    }

    @Test
    void superAdminCanListAllWorkspaceProjects() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(superAdminActor);
        when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspace));
        when(projectRepo.findByWorkspaceWId(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildSavedProject())));

        var result = projectService.getAllProjectsInWorkspace(1, Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }
}
