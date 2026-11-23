package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.CommentResponseDTO;
import com.myapp.worktrack_backend.registration.dto.CreateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.event.ProjectActivityEvent;
import com.myapp.worktrack_backend.registration.exceptionhandler.CommentNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkItemNotFoundException;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemCommentRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final WorkItemCommentRepo commentRepo;
    private final WorkItemRepo workItemRepo;
    private final ProjectRepo projectRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(WorkItemCommentRepo commentRepo,
                          WorkItemRepo workItemRepo,
                          ProjectRepo projectRepo,
                          ProjectPermissionService projectPermissionService,
                          AuthenticatedEmployeeDetails authEmpDetails,
                          ApplicationEventPublisher eventPublisher) {
        this.commentRepo = commentRepo;
        this.workItemRepo = workItemRepo;
        this.projectRepo = projectRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommentResponseDTO addComment(Long projectId, Long workItemId, CreateCommentRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.COMMENT);

        WorkItem workItem = workItemRepo.findByIdAndProjectProjectId(workItemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found: " + workItemId));

        WorkItemComment comment = new WorkItemComment();
        comment.setWorkItem(workItem);
        comment.setAuthor(actor);
        comment.setContent(request.content());

        WorkItemComment saved = commentRepo.save(comment);

        // Publish domain event for audit & notification (if assignee is another employee)
        Long notifyRecipientId = null;
        if (workItem.getAssignee() != null && workItem.getAssignee().getEmployeeId() != actor.getEmployeeId()) {
            notifyRecipientId = workItem.getAssignee().getEmployeeId();
        }

        ProjectActivityEvent event = new ProjectActivityEvent(
                projectId,
                "WORK_ITEM",
                workItemId,
                "COMMENT_ADDED",
                actor.getEmpId(),
                actor.getEmpId() + " commented on work item " + workItem.getItemKey(),
                null,
                request.content().length() > 100 ? request.content().substring(0, 100) + "..." : request.content(),
                notifyRecipientId,
                "New comment on " + workItem.getItemKey(),
                actor.getEmpId() + " added a comment to your assigned item: " + workItem.getTitle(),
                "/projects/" + projectId + "/work-items/" + workItemId
        );
        eventPublisher.publishEvent(event);

        return CommentResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getComments(Long projectId, Long workItemId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        workItemRepo.findByIdAndProjectProjectId(workItemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found: " + workItemId));

        return commentRepo.findByWorkItemIdAndIsDeletedFalseOrderByCreatedAtAsc(workItemId, pageable)
                .map(CommentResponseDTO::fromEntity);
    }

    @Transactional
    public CommentResponseDTO updateComment(Long projectId, Long workItemId, Long commentId, UpdateCommentRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        workItemRepo.findByIdAndProjectProjectId(workItemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found: " + workItemId));

        WorkItemComment comment = commentRepo.findByIdAndWorkItemId(commentId, workItemId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        if (comment.getAuthor().getEmployeeId() != actor.getEmployeeId()) {
            throw new AccessDeniedException("You can only edit your own comments");
        }

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Cannot edit a deleted comment");
        }

        comment.setContent(request.content());
        comment.setEdited(true);

        return CommentResponseDTO.fromEntity(commentRepo.save(comment));
    }

    @Transactional
    public void deleteComment(Long projectId, Long workItemId, Long commentId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        workItemRepo.findByIdAndProjectProjectId(workItemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found: " + workItemId));

        WorkItemComment comment = commentRepo.findByIdAndWorkItemId(commentId, workItemId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        boolean isAuthor = comment.getAuthor().getEmployeeId() == actor.getEmployeeId();
        boolean hasManagerPermission = projectPermissionService.hasPermission(actor, project, ProjectPermission.MANAGE_MEMBERS);

        if (!isAuthor && !hasManagerPermission) {
            throw new AccessDeniedException("You can only delete your own comments unless you have management authority");
        }

        comment.setDeleted(true);
        commentRepo.save(comment);
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }
}
