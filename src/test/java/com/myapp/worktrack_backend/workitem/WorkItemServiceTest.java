package com.myapp.worktrack_backend.workitem;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidHierarchyException;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidWorkItemStatusTransitionException;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkItemNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemRepo;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import com.myapp.worktrack_backend.registration.service.WorkItemService;
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
class WorkItemServiceTest {

    @Mock private WorkItemRepo workItemRepo;
    @Mock private ProjectRepo projectRepo;
    @Mock private EmployeeRepo employeeRepo;
    @Mock private EmployeeWorkspaceRepo employeeWorkspaceRepo;
    @Mock private ProjectPermissionService projectPermissionService;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;

    @InjectMocks
    private WorkItemService workItemService;

    private Employee actor;
    private Employee assignee;
    private Workspace workspace;
    private Project project;

    @BeforeEach
    void setUp() {
        actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM000001");
        actor.setRole("Admin");
        actor.setEmployeeStatus("Active");

        assignee = new Employee();
        assignee.setEmployeeId(2L);
        assignee.setEmpId("EM000002");
        assignee.setRole("Staff");
        assignee.setEmployeeStatus("Active");

        workspace = new Workspace();
        workspace.setWId(1);
        workspace.setWorkspaceName("Main HQ");
        workspace.setStatus("Active");

        project = new Project();
        project.setProjectId(10L);
        project.setProjectKey("PROJ");
        project.setName("Core Platform");
        project.setWorkspace(workspace);
        project.setDeptId("D001");
        project.setStatus("ACTIVE");
    }

    @Test
    void createWorkItem_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findMaxItemNumberByProjectId(10L)).thenReturn(0);
        when(workItemRepo.save(any(WorkItem.class))).thenAnswer(inv -> {
            WorkItem w = inv.getArgument(0);
            w.setId(100L);
            return w;
        });

        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Launch Foundation", "Desc", "MISSION", "HIGH", 10, null, null, null, null, null, null
        );

        WorkItemResponseDTO response = workItemService.createWorkItem(10L, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("PROJ-1", response.itemKey());
        assertEquals(1, response.itemNumber());
        assertEquals("MISSION", response.type());
        assertEquals("BACKLOG", response.status());
        assertEquals("HIGH", response.priority());
        verify(workItemRepo).save(any(WorkItem.class));
    }

    @Test
    void createWorkItem_withValidParentHierarchy_success() {
        WorkItem mission = new WorkItem();
        mission.setId(50L);
        mission.setItemKey("PROJ-1");
        mission.setType(WorkItemType.MISSION);

        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(50L, 10L)).thenReturn(Optional.of(mission));
        when(workItemRepo.findMaxItemNumberByProjectId(10L)).thenReturn(1);
        when(workItemRepo.save(any(WorkItem.class))).thenAnswer(inv -> {
            WorkItem w = inv.getArgument(0);
            w.setId(101L);
            return w;
        });

        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Auth Capability", "Desc", "CAPABILITY", "MEDIUM", 5, 50L, null, null, null, null, null
        );

        WorkItemResponseDTO response = workItemService.createWorkItem(10L, request);

        assertEquals("CAPABILITY", response.type());
        assertEquals(50L, response.parentId());
        assertEquals("PROJ-2", response.itemKey());
    }

    @Test
    void createWorkItem_invalidHierarchy_throwsException() {
        WorkItem workItem = new WorkItem();
        workItem.setId(50L);
        workItem.setType(WorkItemType.WORK_ITEM);

        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(50L, 10L)).thenReturn(Optional.of(workItem));

        // CAPABILITY cannot be a child of WORK_ITEM (must be under MISSION)
        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Invalid Capability", "Desc", "CAPABILITY", "MEDIUM", 5, 50L, null, null, null, null, null
        );

        assertThrows(InvalidHierarchyException.class, () -> workItemService.createWorkItem(10L, request));
    }

    @Test
    void createWorkItem_missionCannotHaveParent() {
        WorkItem parent = new WorkItem();
        parent.setId(50L);
        parent.setType(WorkItemType.MISSION);

        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(50L, 10L)).thenReturn(Optional.of(parent));

        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Mission with parent", "Desc", "MISSION", "HIGH", 5, 50L, null, null, null, null, null
        );

        assertThrows(InvalidHierarchyException.class, () -> workItemService.createWorkItem(10L, request));
    }

    @Test
    void createWorkItem_withAssigneeOutOfScope_rejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(assignee));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(0L);

        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Task", "Desc", "WORK_ITEM", "LOW", 3, null, "EM000002", null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> workItemService.createWorkItem(10L, request));
    }

    @Test
    void createWorkItem_withoutPermission_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("Denied"))
                .when(projectPermissionService).assertPermission(actor, project, ProjectPermission.CREATE_WORK_ITEMS);

        CreateWorkItemRequestDTO request = new CreateWorkItemRequestDTO(
                "Task", "Desc", "WORK_ITEM", "LOW", 3, null, null, null, null, null, null
        );

        assertThrows(AccessDeniedException.class, () -> workItemService.createWorkItem(10L, request));
    }

    @Test
    void getWorkItem_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setItemKey("PROJ-1");
        item.setItemNumber(1);
        item.setTitle("Build API");
        item.setType(WorkItemType.WORK_ITEM);
        item.setStatus(WorkItemStatus.READY);
        item.setReporterEmpId("EM000001");
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));

        WorkItemResponseDTO response = workItemService.getWorkItem(10L, 100L);

        assertNotNull(response);
        assertEquals("PROJ-1", response.itemKey());
        assertEquals("Build API", response.title());
    }

    @Test
    void getWorkItem_notFound_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(999L, 10L)).thenReturn(Optional.empty());

        assertThrows(WorkItemNotFoundException.class, () -> workItemService.getWorkItem(10L, 999L));
    }

    @Test
    void updateWorkItem_byReporter_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setReporterEmpId("EM000001");
        item.setTitle("Old Title");
        item.setType(WorkItemType.WORK_ITEM);
        item.setStatus(WorkItemStatus.BACKLOG);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));
        when(workItemRepo.save(any(WorkItem.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateWorkItemRequestDTO request = new UpdateWorkItemRequestDTO(
                "New Title", "New Desc", "CRITICAL", 8, null, null, null, null
        );

        WorkItemResponseDTO response = workItemService.updateWorkItem(10L, 100L, request);

        assertEquals("New Title", response.title());
        assertEquals("CRITICAL", response.priority());
        assertEquals(8, response.estimationPoints());
        verify(projectPermissionService).assertPermission(actor, project, ProjectPermission.UPDATE_OWN_WORK_ITEMS);
    }

    @Test
    void updateWorkItem_selfParent_rejected() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setReporterEmpId("EM000001");
        item.setType(WorkItemType.WORK_ITEM);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));

        UpdateWorkItemRequestDTO request = new UpdateWorkItemRequestDTO(
                null, null, null, null, 100L, null, null, null
        );

        assertThrows(InvalidHierarchyException.class, () -> workItemService.updateWorkItem(10L, 100L, request));
    }

    @Test
    void updateStatus_validTransition_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setStatus(WorkItemStatus.BACKLOG);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));
        when(workItemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChangeWorkItemStatusRequestDTO request = new ChangeWorkItemStatusRequestDTO("READY");
        WorkItemResponseDTO response = workItemService.updateStatus(10L, 100L, request);

        assertEquals("READY", response.status());
    }

    @Test
    void updateStatus_invalidTransition_throwsException() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setStatus(WorkItemStatus.BACKLOG);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));

        // BACKLOG directly to COMPLETED is not allowed
        ChangeWorkItemStatusRequestDTO request = new ChangeWorkItemStatusRequestDTO("COMPLETED");
        assertThrows(InvalidWorkItemStatusTransitionException.class, () -> workItemService.updateStatus(10L, 100L, request));
    }

    @Test
    void assignWorkItem_unassign_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        item.setAssignee(assignee);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));
        when(workItemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AssignWorkItemRequestDTO request = new AssignWorkItemRequestDTO(null);
        WorkItemResponseDTO response = workItemService.assignWorkItem(10L, 100L, request);

        assertNull(response.assigneeEmpId());
    }

    @Test
    void assignWorkItem_newAssignee_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(employeeRepo.findByEmpId("EM000002")).thenReturn(Optional.of(assignee));
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment("EM000002", 1, "D001")).thenReturn(1L);

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(item));
        when(workItemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AssignWorkItemRequestDTO request = new AssignWorkItemRequestDTO("EM000002");
        WorkItemResponseDTO response = workItemService.assignWorkItem(10L, 100L, request);

        assertEquals("EM000002", response.assigneeEmpId());
    }

    @Test
    void getBacklog_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem item = new WorkItem();
        item.setId(100L);
        item.setProject(project);
        when(workItemRepo.findByProjectProjectIdAndSprintIdIsNull(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        Page<WorkItemResponseDTO> result = workItemService.getBacklog(10L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getChildren_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        WorkItem parent = new WorkItem();
        parent.setId(50L);
        parent.setProject(project);

        WorkItem child = new WorkItem();
        child.setId(100L);
        child.setProject(project);
        child.setParent(parent);

        when(workItemRepo.findByIdAndProjectProjectId(50L, 10L)).thenReturn(Optional.of(parent));
        when(workItemRepo.findByParentId(50L)).thenReturn(List.of(child));

        List<WorkItemResponseDTO> children = workItemService.getChildren(10L, 50L);

        assertEquals(1, children.size());
    }
}
