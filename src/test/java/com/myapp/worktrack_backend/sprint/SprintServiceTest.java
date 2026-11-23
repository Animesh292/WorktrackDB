package com.myapp.worktrack_backend.sprint;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.ActiveSprintExistsException;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidSprintStateTransitionException;
import com.myapp.worktrack_backend.registration.exceptionhandler.SprintNotFoundException;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.repository.SprintRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemRepo;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import com.myapp.worktrack_backend.registration.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock private SprintRepo sprintRepo;
    @Mock private ProjectRepo projectRepo;
    @Mock private WorkItemRepo workItemRepo;
    @Mock private ProjectPermissionService projectPermissionService;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;

    @InjectMocks
    private SprintService sprintService;

    private Employee actor;
    private Workspace workspace;
    private Project project;

    @BeforeEach
    void setUp() {
        actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM000001");
        actor.setRole("Admin");
        actor.setEmployeeStatus("Active");

        workspace = new Workspace();
        workspace.setWId(1);
        workspace.setWorkspaceName("HQ Workspace");

        project = new Project();
        project.setProjectId(10L);
        project.setProjectKey("PROJ");
        project.setName("WorkTrack Core");
        project.setWorkspace(workspace);
        project.setDeptId("D001");
    }

    @Test
    void createSprint_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(sprintRepo.save(any(Sprint.class))).thenAnswer(inv -> {
            Sprint s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        CreateSprintRequestDTO request = new CreateSprintRequestDTO(
                "Sprint 1", "Launch Alpha", LocalDate.now(), LocalDate.now().plusWeeks(2)
        );

        SprintResponseDTO response = sprintService.createSprint(10L, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Sprint 1", response.name());
        assertEquals("PLANNED", response.state());
        assertEquals("EM000001", response.createdByEmpId());
        verify(sprintRepo).save(any(Sprint.class));
    }

    @Test
    void createSprint_invalidDates_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        CreateSprintRequestDTO request = new CreateSprintRequestDTO(
                "Sprint Bad Dates", "Goal", LocalDate.now().plusWeeks(2), LocalDate.now()
        );

        assertThrows(IllegalArgumentException.class, () -> sprintService.createSprint(10L, request));
    }

    @Test
    void createSprint_withoutPermission_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("Denied"))
                .when(projectPermissionService).assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        CreateSprintRequestDTO request = new CreateSprintRequestDTO(
                "Sprint 1", "Goal", LocalDate.now(), LocalDate.now().plusWeeks(2)
        );

        assertThrows(AccessDeniedException.class, () -> sprintService.createSprint(10L, request));
    }

    @Test
    void getSprint_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setName("Sprint 1");
        sprint.setStartDate(LocalDate.now());
        sprint.setEndDate(LocalDate.now().plusWeeks(2));
        sprint.setState(SprintState.PLANNED);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        SprintResponseDTO response = sprintService.getSprint(10L, 100L);

        assertNotNull(response);
        assertEquals("Sprint 1", response.name());
    }

    @Test
    void getActiveSprint_found() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint active = new Sprint();
        active.setId(100L);
        active.setProject(project);
        active.setName("Sprint Active");
        active.setState(SprintState.ACTIVE);
        when(sprintRepo.findByProjectProjectIdAndState(10L, SprintState.ACTIVE)).thenReturn(Optional.of(active));

        SprintResponseDTO response = sprintService.getActiveSprint(10L);

        assertEquals("ACTIVE", response.state());
        assertEquals("Sprint Active", response.name());
    }

    @Test
    void getActiveSprint_notFound_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(sprintRepo.findByProjectProjectIdAndState(10L, SprintState.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(SprintNotFoundException.class, () -> sprintService.getActiveSprint(10L));
    }

    @Test
    void startSprint_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setName("Sprint 1");
        sprint.setState(SprintState.PLANNED);

        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));
        when(sprintRepo.existsByProjectProjectIdAndState(10L, SprintState.ACTIVE)).thenReturn(false);
        when(sprintRepo.save(any(Sprint.class))).thenAnswer(inv -> inv.getArgument(0));

        SprintResponseDTO response = sprintService.startSprint(10L, 100L);

        assertEquals("ACTIVE", response.state());
        verify(sprintRepo).save(sprint);
    }

    @Test
    void startSprint_anotherActiveSprintExists_rejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.PLANNED);

        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));
        when(sprintRepo.existsByProjectProjectIdAndState(10L, SprintState.ACTIVE)).thenReturn(true);

        assertThrows(ActiveSprintExistsException.class, () -> sprintService.startSprint(10L, 100L));
        verify(sprintRepo, never()).save(any());
    }

    @Test
    void startSprint_notPlannedState_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.ACTIVE);

        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        assertThrows(InvalidSprintStateTransitionException.class, () -> sprintService.startSprint(10L, 100L));
    }

    @Test
    void completeSprint_withoutTargetSprint_unfinishedMovedToBacklog() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.ACTIVE);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        WorkItem finishedItem = new WorkItem();
        finishedItem.setId(1L);
        finishedItem.setSprintId(100L);
        finishedItem.setStatus(WorkItemStatus.COMPLETED);

        WorkItem unfinishedItem = new WorkItem();
        unfinishedItem.setId(2L);
        unfinishedItem.setSprintId(100L);
        unfinishedItem.setStatus(WorkItemStatus.IN_PROGRESS);

        when(workItemRepo.findByProjectProjectIdAndSprintId(10L, 100L))
                .thenReturn(List.of(finishedItem, unfinishedItem));
        when(sprintRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SprintResponseDTO response = sprintService.completeSprint(10L, 100L, new CompleteSprintRequestDTO(null));

        assertEquals("COMPLETED", response.state());
        // finished item sprintId remains 100
        assertEquals(100L, finishedItem.getSprintId());
        // unfinished item sprintId moved to null (backlog)
        assertNull(unfinishedItem.getSprintId());
        verify(workItemRepo).save(unfinishedItem);
        verify(workItemRepo, never()).save(finishedItem);
    }

    @Test
    void completeSprint_withTargetSprint_unfinishedMovedToTargetSprint() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.ACTIVE);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        Sprint targetSprint = new Sprint();
        targetSprint.setId(101L);
        targetSprint.setProject(project);
        targetSprint.setState(SprintState.PLANNED);
        when(sprintRepo.findByIdAndProjectProjectId(101L, 10L)).thenReturn(Optional.of(targetSprint));

        WorkItem unfinished = new WorkItem();
        unfinished.setId(5L);
        unfinished.setSprintId(100L);
        unfinished.setStatus(WorkItemStatus.READY);

        when(workItemRepo.findByProjectProjectIdAndSprintId(10L, 100L)).thenReturn(List.of(unfinished));
        when(sprintRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SprintResponseDTO response = sprintService.completeSprint(10L, 100L, new CompleteSprintRequestDTO(101L));

        assertEquals("COMPLETED", response.state());
        assertEquals(101L, unfinished.getSprintId());
        verify(workItemRepo).save(unfinished);
    }

    @Test
    void cancelSprint_success_allWorkItemsMovedToBacklog() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.PLANNED);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        WorkItem item = new WorkItem();
        item.setId(1L);
        item.setSprintId(100L);

        when(workItemRepo.findByProjectProjectIdAndSprintId(10L, 100L)).thenReturn(List.of(item));
        when(sprintRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SprintResponseDTO response = sprintService.cancelSprint(10L, 100L);

        assertEquals("CANCELLED", response.state());
        assertNull(item.getSprintId());
        verify(workItemRepo).save(item);
    }

    @Test
    void addWorkItemsToSprint_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        sprint.setState(SprintState.PLANNED);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        WorkItem item1 = new WorkItem();
        item1.setId(10L);
        item1.setProject(project);
        WorkItem item2 = new WorkItem();
        item2.setId(20L);
        item2.setProject(project);

        when(workItemRepo.findByIdAndProjectProjectId(10L, 10L)).thenReturn(Optional.of(item1));
        when(workItemRepo.findByIdAndProjectProjectId(20L, 10L)).thenReturn(Optional.of(item2));
        when(workItemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddWorkItemsToSprintRequestDTO request = new AddWorkItemsToSprintRequestDTO(List.of(10L, 20L));
        List<WorkItemResponseDTO> results = sprintService.addWorkItemsToSprint(10L, 100L, request);

        assertEquals(2, results.size());
        assertEquals(100L, item1.getSprintId());
        assertEquals(100L, item2.getSprintId());
    }

    @Test
    void removeWorkItemFromSprint_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        Sprint sprint = new Sprint();
        sprint.setId(100L);
        sprint.setProject(project);
        when(sprintRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(sprint));

        WorkItem item = new WorkItem();
        item.setId(10L);
        item.setProject(project);
        item.setSprintId(100L);
        when(workItemRepo.findByIdAndProjectProjectId(10L, 10L)).thenReturn(Optional.of(item));
        when(workItemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkItemResponseDTO response = sprintService.removeWorkItemFromSprint(10L, 100L, 10L);

        assertNull(response.sprintId());
        assertNull(item.getSprintId());
    }
}
