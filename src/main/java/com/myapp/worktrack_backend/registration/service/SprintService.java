package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.ActiveSprintExistsException;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidSprintStateTransitionException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.SprintNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkItemNotFoundException;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.repository.SprintRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SprintService {

    private final SprintRepo sprintRepo;
    private final ProjectRepo projectRepo;
    private final WorkItemRepo workItemRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public SprintService(SprintRepo sprintRepo,
                         ProjectRepo projectRepo,
                         WorkItemRepo workItemRepo,
                         ProjectPermissionService projectPermissionService,
                         AuthenticatedEmployeeDetails authEmpDetails) {
        this.sprintRepo = sprintRepo;
        this.projectRepo = projectRepo;
        this.workItemRepo = workItemRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public SprintResponseDTO createSprint(Long projectId, CreateSprintRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Sprint end date must be on or after start date");
        }

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setState(SprintState.PLANNED);
        sprint.setCreatedByEmpId(actor.getEmpId());

        return SprintResponseDTO.fromEntity(sprintRepo.save(sprint));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SprintResponseDTO getSprint(Long projectId, Long sprintId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        return SprintResponseDTO.fromEntity(sprint);
    }

    @Transactional(readOnly = true)
    public SprintResponseDTO getActiveSprint(Long projectId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        Sprint activeSprint = sprintRepo.findByProjectProjectIdAndState(projectId, SprintState.ACTIVE)
                .orElseThrow(() -> new SprintNotFoundException("No active sprint for project ID: " + projectId));

        return SprintResponseDTO.fromEntity(activeSprint);
    }

    @Transactional(readOnly = true)
    public Page<SprintResponseDTO> getSprints(Long projectId, String state, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        if (state != null && !state.isBlank()) {
            SprintState sprintState = SprintState.valueOf(state);
            return sprintRepo.findByProjectProjectIdAndState(projectId, sprintState, pageable)
                    .map(SprintResponseDTO::fromEntity);
        }

        return sprintRepo.findByProjectProjectId(projectId, pageable)
                .map(SprintResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponseDTO> getSprintWorkItems(Long projectId, Long sprintId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        return workItemRepo.findByProjectProjectIdAndSprintId(projectId, sprintId, pageable)
                .map(WorkItemResponseDTO::fromEntity);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public SprintResponseDTO updateSprint(Long projectId, Long sprintId, UpdateSprintRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (sprint.getState() == SprintState.COMPLETED || sprint.getState() == SprintState.CANCELLED) {
            throw new IllegalArgumentException("Cannot update a completed or cancelled sprint");
        }

        if (request.name() != null && !request.name().isBlank()) {
            sprint.setName(request.name());
        }
        if (request.goal() != null) {
            sprint.setGoal(request.goal());
        }
        if (request.startDate() != null) {
            sprint.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            sprint.setEndDate(request.endDate());
        }

        if (sprint.getEndDate().isBefore(sprint.getStartDate())) {
            throw new IllegalArgumentException("Sprint end date must be on or after start date");
        }

        return SprintResponseDTO.fromEntity(sprintRepo.save(sprint));
    }

    // ── LIFECYCLE ─────────────────────────────────────────────────────────────

    @Transactional
    public SprintResponseDTO startSprint(Long projectId, Long sprintId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (sprint.getState() != SprintState.PLANNED) {
            throw new InvalidSprintStateTransitionException(
                    "Only PLANNED sprints can be started. Current state: " + sprint.getState());
        }

        // Invariant: Exactly ONE active sprint per project
        boolean hasActive = sprintRepo.existsByProjectProjectIdAndState(projectId, SprintState.ACTIVE);
        if (hasActive) {
            throw new ActiveSprintExistsException(
                    "Project already has an active sprint. Complete or cancel it before starting another.");
        }

        sprint.setState(SprintState.ACTIVE);
        return SprintResponseDTO.fromEntity(sprintRepo.save(sprint));
    }

    @Transactional
    public SprintResponseDTO completeSprint(Long projectId, Long sprintId, CompleteSprintRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (sprint.getState() != SprintState.ACTIVE) {
            throw new InvalidSprintStateTransitionException(
                    "Only ACTIVE sprints can be completed. Current state: " + sprint.getState());
        }

        // Handle unfinished work items in this sprint
        List<WorkItem> sprintItems = workItemRepo.findByProjectProjectIdAndSprintId(projectId, sprintId);
        Long targetSprintId = request != null ? request.targetSprintId() : null;

        if (targetSprintId != null) {
            Sprint targetSprint = sprintRepo.findByIdAndProjectProjectId(targetSprintId, projectId)
                    .orElseThrow(() -> new SprintNotFoundException("Target sprint not found with id: " + targetSprintId));
            if (targetSprint.getState() != SprintState.PLANNED) {
                throw new IllegalArgumentException("Target sprint for roll-over must be in PLANNED state");
            }
        }

        for (WorkItem item : sprintItems) {
            boolean isFinished = item.getStatus() == WorkItemStatus.COMPLETED
                    || item.getStatus() == WorkItemStatus.CANCELLED;
            if (!isFinished) {
                // Move unfinished item to target sprint or back to backlog
                item.setSprintId(targetSprintId);
                workItemRepo.save(item);
            }
        }

        sprint.setState(SprintState.COMPLETED);
        return SprintResponseDTO.fromEntity(sprintRepo.save(sprint));
    }

    @Transactional
    public SprintResponseDTO cancelSprint(Long projectId, Long sprintId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (!sprint.getState().canTransitionTo(SprintState.CANCELLED)) {
            throw new InvalidSprintStateTransitionException(
                    "Cannot cancel sprint in state: " + sprint.getState());
        }

        // Move all items back to backlog
        List<WorkItem> sprintItems = workItemRepo.findByProjectProjectIdAndSprintId(projectId, sprintId);
        for (WorkItem item : sprintItems) {
            item.setSprintId(null);
            workItemRepo.save(item);
        }

        sprint.setState(SprintState.CANCELLED);
        return SprintResponseDTO.fromEntity(sprintRepo.save(sprint));
    }

    // ── WORK ITEM SPRINT ASSOCIATION ──────────────────────────────────────────

    @Transactional
    public List<WorkItemResponseDTO> addWorkItemsToSprint(Long projectId, Long sprintId, AddWorkItemsToSprintRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        Sprint sprint = sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (sprint.getState() == SprintState.COMPLETED || sprint.getState() == SprintState.CANCELLED) {
            throw new IllegalArgumentException("Cannot add work items to a completed or cancelled sprint");
        }

        List<WorkItemResponseDTO> results = new ArrayList<>();
        for (Long itemId : request.workItemIds()) {
            WorkItem item = workItemRepo.findByIdAndProjectProjectId(itemId, projectId)
                    .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + itemId + " in project " + projectId));
            item.setSprintId(sprintId);
            results.add(WorkItemResponseDTO.fromEntity(workItemRepo.save(item)));
        }

        return results;
    }

    @Transactional
    public WorkItemResponseDTO removeWorkItemFromSprint(Long projectId, Long sprintId, Long workItemId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_SPRINTS);

        sprintRepo.findByIdAndProjectProjectId(sprintId, projectId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        WorkItem item = workItemRepo.findByIdAndProjectProjectId(workItemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + workItemId));

        if (item.getSprintId() != null && item.getSprintId().equals(sprintId)) {
            item.setSprintId(null);
            return WorkItemResponseDTO.fromEntity(workItemRepo.save(item));
        }

        return WorkItemResponseDTO.fromEntity(item);
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }
}
