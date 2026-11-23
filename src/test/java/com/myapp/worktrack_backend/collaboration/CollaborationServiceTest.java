package com.myapp.worktrack_backend.collaboration;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.CommentResponseDTO;
import com.myapp.worktrack_backend.registration.dto.CreateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.dto.InAppNotificationResponseDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.event.ProjectActivityEvent;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.service.ActivityLogService;
import com.myapp.worktrack_backend.registration.service.CommentService;
import com.myapp.worktrack_backend.registration.service.NotificationService;
import com.myapp.worktrack_backend.registration.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
class CollaborationServiceTest {

    @Mock private WorkItemCommentRepo commentRepo;
    @Mock private WorkItemRepo workItemRepo;
    @Mock private ProjectRepo projectRepo;
    @Mock private ActivityLogRepo activityLogRepo;
    @Mock private InAppNotificationRepo notificationRepo;
    @Mock private EmployeeRepo employeeRepo;
    @Mock private ProjectPermissionService projectPermissionService;
    @Mock private AuthenticatedEmployeeDetails authEmpDetails;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CommentService commentService;
    private ActivityLogService activityLogService;
    private NotificationService notificationService;

    private Employee actor;
    private Employee otherUser;
    private Project project;
    private WorkItem workItem;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
                commentRepo, workItemRepo, projectRepo, projectPermissionService, authEmpDetails, eventPublisher
        );
        activityLogService = new ActivityLogService(
                activityLogRepo, projectRepo, projectPermissionService, authEmpDetails
        );
        notificationService = new NotificationService(
                notificationRepo, employeeRepo, authEmpDetails
        );

        actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM000001");
        actor.setRole("Staff");
        actor.setEmployeeStatus("Active");

        otherUser = new Employee();
        otherUser.setEmployeeId(2L);
        otherUser.setEmpId("EM000002");
        otherUser.setRole("Staff");
        otherUser.setEmployeeStatus("Active");

        project = new Project();
        project.setProjectId(10L);
        project.setProjectKey("PROJ");
        project.setName("WorkTrack");

        workItem = new WorkItem();
        workItem.setId(100L);
        workItem.setProject(project);
        workItem.setItemKey("PROJ-1");
        workItem.setTitle("Task 1");
        workItem.setAssignee(otherUser);
    }

    // ── COMMENT TESTS ─────────────────────────────────────────────────────────

    @Test
    void addComment_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));
        when(commentRepo.save(any(WorkItemComment.class))).thenAnswer(inv -> {
            WorkItemComment c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });

        CreateCommentRequestDTO request = new CreateCommentRequestDTO("Great progress on this!");
        CommentResponseDTO response = commentService.addComment(10L, 100L, request);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Great progress on this!", response.content());
        assertEquals("EM000001", response.authorEmpId());
        verify(commentRepo).save(any(WorkItemComment.class));
        verify(eventPublisher).publishEvent(any(ProjectActivityEvent.class));
    }

    @Test
    void addComment_withoutPermission_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("Denied"))
                .when(projectPermissionService).assertPermission(actor, project, ProjectPermission.COMMENT);

        CreateCommentRequestDTO request = new CreateCommentRequestDTO("Comment");
        assertThrows(AccessDeniedException.class, () -> commentService.addComment(10L, 100L, request));
    }

    @Test
    void getComments_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));

        WorkItemComment c = new WorkItemComment();
        c.setId(1L);
        c.setWorkItem(workItem);
        c.setAuthor(actor);
        c.setContent("Hello");

        when(commentRepo.findByWorkItemIdAndIsDeletedFalseOrderByCreatedAtAsc(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(c)));

        Page<CommentResponseDTO> result = commentService.getComments(10L, 100L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().get(0).content());
    }

    @Test
    void updateComment_byAuthor_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));

        WorkItemComment c = new WorkItemComment();
        c.setId(1L);
        c.setWorkItem(workItem);
        c.setAuthor(actor);
        c.setContent("Old text");
        when(commentRepo.findByIdAndWorkItemId(1L, 100L)).thenReturn(Optional.of(c));
        when(commentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCommentRequestDTO request = new UpdateCommentRequestDTO("Updated text");
        CommentResponseDTO response = commentService.updateComment(10L, 100L, 1L, request);

        assertEquals("Updated text", response.content());
        assertTrue(response.isEdited());
    }

    @Test
    void updateComment_byNonAuthor_denied() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));

        WorkItemComment c = new WorkItemComment();
        c.setId(1L);
        c.setWorkItem(workItem);
        c.setAuthor(otherUser); // author is otherUser, actor is actor
        when(commentRepo.findByIdAndWorkItemId(1L, 100L)).thenReturn(Optional.of(c));

        UpdateCommentRequestDTO request = new UpdateCommentRequestDTO("Hacked text");
        assertThrows(AccessDeniedException.class,
                () -> commentService.updateComment(10L, 100L, 1L, request));
    }

    @Test
    void deleteComment_byAuthor_softDeleted() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));

        WorkItemComment c = new WorkItemComment();
        c.setId(1L);
        c.setWorkItem(workItem);
        c.setAuthor(actor);
        when(commentRepo.findByIdAndWorkItemId(1L, 100L)).thenReturn(Optional.of(c));

        commentService.deleteComment(10L, 100L, 1L);

        assertTrue(c.isDeleted());
        verify(commentRepo).save(c);
    }

    @Test
    void deleteComment_byManager_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(workItemRepo.findByIdAndProjectProjectId(100L, 10L)).thenReturn(Optional.of(workItem));

        WorkItemComment c = new WorkItemComment();
        c.setId(1L);
        c.setWorkItem(workItem);
        c.setAuthor(otherUser);
        when(commentRepo.findByIdAndWorkItemId(1L, 100L)).thenReturn(Optional.of(c));
        when(projectPermissionService.hasPermission(actor, project, ProjectPermission.MANAGE_MEMBERS)).thenReturn(true);

        commentService.deleteComment(10L, 100L, 1L);

        assertTrue(c.isDeleted());
        verify(commentRepo).save(c);
    }

    // ── ACTIVITY LOG TESTS ────────────────────────────────────────────────────

    @Test
    void handleActivityEvent_savesLogEntry() {
        ProjectActivityEvent event = ProjectActivityEvent.forAuditOnly(
                10L, "WORK_ITEM", 100L, "STATUS_CHANGE", "EM000001",
                "Changed status to IN_PROGRESS", "BACKLOG", "IN_PROGRESS"
        );

        activityLogService.handleActivityEvent(event);

        verify(activityLogRepo).save(argThat(log ->
                log.getProjectId().equals(10L) &&
                log.getAction().equals("STATUS_CHANGE") &&
                log.getActorEmpId().equals("EM000001") &&
                log.getNewValue().equals("IN_PROGRESS")
        ));
    }

    // ── NOTIFICATION TESTS ────────────────────────────────────────────────────

    @Test
    void handleNotificationEvent_createsNotificationForRecipient() {
        when(employeeRepo.findById(2L)).thenReturn(Optional.of(otherUser));

        ProjectActivityEvent event = new ProjectActivityEvent(
                10L, "WORK_ITEM", 100L, "ASSIGN", "EM000001",
                "Assigned to you", null, null,
                2L, "Work Assigned", "You were assigned to task", "/link"
        );

        notificationService.handleNotificationEvent(event);

        verify(notificationRepo).save(argThat(n ->
                n.getRecipient().equals(otherUser) &&
                n.getTitle().equals("Work Assigned") &&
                !n.isRead()
        ));
    }

    @Test
    void markAsRead_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);

        InAppNotification n = new InAppNotification();
        n.setId(99L);
        n.setRecipient(actor);
        n.setRead(false);
        when(notificationRepo.findByIdAndRecipientEmployeeId(99L, 1L)).thenReturn(Optional.of(n));
        when(notificationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InAppNotificationResponseDTO response = notificationService.markAsRead(99L);

        assertTrue(response.isRead());
        verify(notificationRepo).save(n);
    }

    @Test
    void getUnreadCount_success() {
        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(notificationRepo.countByRecipientEmployeeIdAndIsReadFalse(1L)).thenReturn(3L);

        assertEquals(3L, notificationService.getUnreadCount());
    }
}
