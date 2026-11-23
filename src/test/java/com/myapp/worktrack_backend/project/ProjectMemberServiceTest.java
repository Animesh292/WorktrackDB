package com.myapp.worktrack_backend.project;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.AddProjectMemberRequestDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectMemberResponseDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateMemberRoleRequestDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.DuplicateMembershipException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectMemberNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectMemberRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.service.ProjectMemberService;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock private ProjectMemberRepo projectMemberRepo;
    @Mock private ProjectRepo projectRepo;
    @Mock private EmployeeRepo employeeRepo;
    @Mock private EmployeeWorkspaceRepo employeeWorkspaceRepo;
    @Mock private ProjectPermissionService projectPermissionService;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private Employee actorAdmin;
    private Employee targetStaff;
    private Workspace workspace;
    private Project project;

    @BeforeEach
    void setUp() {
        actorAdmin = new Employee();
        actorAdmin.setEmployeeId(1L);
        actorAdmin.setEmpId("EM000001");
        actorAdmin.setRole("Admin");
        actorAdmin.setEmployeeStatus("Active");

        targetStaff = new Employee();
        targetStaff.setEmployeeId(2L);
        targetStaff.setEmpId("EM000002");
        targetStaff.setRole("Staff");
        targetStaff.setEmployeeStatus("Active");

        workspace = new Workspace();
        workspace.setWId(1);
        workspace.setWorkspaceName("HQ Workspace");
        workspace.setStatus("Active");

        project = new Project();
        project.setProjectId(100L);
        project.setProjectKey("PROJ-01");
        project.setName("WorkTrack V1");
        project.setWorkspace(workspace);
        project.setDeptId("D001");
        project.setStatus("ACTIVE");
    }

    @Test
    void addMember_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(targetStaff));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(1L);
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeId(100L, 2L)).thenReturn(Optional.empty());
        when(projectMemberRepo.save(any(ProjectMember.class))).thenAnswer(inv -> {
            ProjectMember m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "CONTRIBUTOR");
        ProjectMemberResponseDTO response = projectMemberService.addMember(100L, dto);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("CONTRIBUTOR", response.role());
        assertEquals("ACTIVE", response.status());
        assertEquals("EM000002", response.empId());
        verify(projectMemberRepo).save(any(ProjectMember.class));
    }

    @Test
    void addMember_duplicateActiveMember_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(targetStaff));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(1L);

        ProjectMember existing = new ProjectMember();
        existing.setId(5L);
        existing.setStatus("ACTIVE");
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeId(100L, 2L)).thenReturn(Optional.of(existing));

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "CONTRIBUTOR");
        assertThrows(DuplicateMembershipException.class, () -> projectMemberService.addMember(100L, dto));
        verify(projectMemberRepo, never()).save(any());
    }

    @Test
    void addMember_reactivatesPreviouslyRemovedMember() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(targetStaff));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(1L);

        ProjectMember existing = new ProjectMember();
        existing.setId(5L);
        existing.setProject(project);
        existing.setEmployee(targetStaff);
        existing.setStatus("REMOVED");
        existing.setRole(ProjectMemberRole.VIEWER);
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeId(100L, 2L)).thenReturn(Optional.of(existing));
        when(projectMemberRepo.save(any(ProjectMember.class))).thenAnswer(inv -> inv.getArgument(0));

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "MANAGER");
        ProjectMemberResponseDTO response = projectMemberService.addMember(100L, dto);

        assertEquals("ACTIVE", response.status());
        assertEquals("MANAGER", response.role());
        verify(projectMemberRepo).save(existing);
    }

    @Test
    void addMember_inactiveTargetEmployee_rejected() {
        targetStaff.setEmployeeStatus("Inactive");
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(targetStaff));

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "CONTRIBUTOR");
        assertThrows(IllegalArgumentException.class, () -> projectMemberService.addMember(100L, dto));
    }

    @Test
    void addMember_targetOutsideWorkspaceDept_rejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(targetStaff));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(0L);

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "CONTRIBUTOR");
        assertThrows(IllegalArgumentException.class, () -> projectMemberService.addMember(100L, dto));
    }

    @Test
    void addMember_actorWithoutPermission_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("Denied"))
                .when(projectPermissionService).assertPermission(actorAdmin, project, ProjectPermission.MANAGE_MEMBERS);

        AddProjectMemberRequestDTO dto = new AddProjectMemberRequestDTO("EM000002", "CONTRIBUTOR");
        assertThrows(AccessDeniedException.class, () -> projectMemberService.addMember(100L, dto));
    }

    @Test
    void removeMember_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));

        ProjectMember member = new ProjectMember();
        member.setId(10L);
        member.setProject(project);
        member.setEmployee(targetStaff);
        member.setStatus("ACTIVE");
        when(projectMemberRepo.findByIdAndProjectProjectId(10L, 100L)).thenReturn(Optional.of(member));
        when(projectMemberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectMemberResponseDTO response = projectMemberService.removeMember(100L, 10L);

        assertEquals("REMOVED", response.status());
        verify(projectMemberRepo).save(member);
    }

    @Test
    void removeMember_notFound_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(projectMemberRepo.findByIdAndProjectProjectId(999L, 100L)).thenReturn(Optional.empty());

        assertThrows(ProjectMemberNotFoundException.class, () -> projectMemberService.removeMember(100L, 999L));
    }

    @Test
    void updateMemberRole_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));

        ProjectMember member = new ProjectMember();
        member.setId(10L);
        member.setProject(project);
        member.setEmployee(targetStaff);
        member.setStatus("ACTIVE");
        member.setRole(ProjectMemberRole.CONTRIBUTOR);
        when(projectMemberRepo.findByIdAndProjectProjectId(10L, 100L)).thenReturn(Optional.of(member));
        when(projectMemberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateMemberRoleRequestDTO dto = new UpdateMemberRoleRequestDTO("MANAGER");
        ProjectMemberResponseDTO response = projectMemberService.updateMemberRole(100L, 10L, dto);

        assertEquals("MANAGER", response.role());
    }

    @Test
    void updateMemberRole_removedMember_rejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));

        ProjectMember member = new ProjectMember();
        member.setId(10L);
        member.setProject(project);
        member.setEmployee(targetStaff);
        member.setStatus("REMOVED");
        when(projectMemberRepo.findByIdAndProjectProjectId(10L, 100L)).thenReturn(Optional.of(member));

        UpdateMemberRoleRequestDTO dto = new UpdateMemberRoleRequestDTO("MANAGER");
        assertThrows(IllegalArgumentException.class, () -> projectMemberService.updateMemberRole(100L, 10L, dto));
    }

    @Test
    void getProjectMembers_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actorAdmin);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));

        ProjectMember member = new ProjectMember();
        member.setId(10L);
        member.setProject(project);
        member.setEmployee(targetStaff);
        member.setRole(ProjectMemberRole.CONTRIBUTOR);
        member.setStatus("ACTIVE");

        when(projectMemberRepo.findByProjectProjectIdAndStatus(eq(100L), eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(member)));

        Page<ProjectMemberResponseDTO> result = projectMemberService.getProjectMembers(100L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("CONTRIBUTOR", result.getContent().get(0).role());
    }

    @Test
    void getMyMembership_found() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(targetStaff);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));

        ProjectMember member = new ProjectMember();
        member.setId(10L);
        member.setProject(project);
        member.setEmployee(targetStaff);
        member.setRole(ProjectMemberRole.CONTRIBUTOR);
        member.setStatus("ACTIVE");

        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(100L, 2L, "ACTIVE"))
                .thenReturn(Optional.of(member));

        ProjectMemberResponseDTO response = projectMemberService.getMyMembership(100L);

        assertNotNull(response);
        assertEquals("EM000002", response.empId());
        assertEquals("CONTRIBUTOR", response.role());
    }

    @Test
    void getMyMembership_notFound_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(targetStaff);
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(100L, 2L, "ACTIVE"))
                .thenReturn(Optional.empty());

        assertThrows(ProjectMemberNotFoundException.class, () -> projectMemberService.getMyMembership(100L));
    }
}
