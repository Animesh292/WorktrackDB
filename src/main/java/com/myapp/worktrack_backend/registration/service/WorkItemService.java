package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidHierarchyException;
import com.myapp.worktrack_backend.registration.exceptionhandler.InvalidWorkItemStatusTransitionException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkItemNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import com.myapp.worktrack_backend.registration.repository.WorkItemRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkItemService {

    private final WorkItemRepo workItemRepo;
    private final ProjectRepo projectRepo;
    private final EmployeeRepo employeeRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public WorkItemService(WorkItemRepo workItemRepo,
                           ProjectRepo projectRepo,
                           EmployeeRepo employeeRepo,
                           EmployeeWorkspaceRepo employeeWorkspaceRepo,
                           ProjectPermissionService projectPermissionService,
                           AuthenticatedEmployeeDetails authEmpDetails) {
        this.workItemRepo = workItemRepo;
        this.projectRepo = projectRepo;
        this.employeeRepo = employeeRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public WorkItemResponseDTO createWorkItem(Long projectId, CreateWorkItemRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.CREATE_WORK_ITEMS);

        WorkItemType type = WorkItemType.valueOf(request.type());
        WorkItem parent = null;
        if (request.parentId() != null) {
            parent = workItemRepo.findByIdAndProjectProjectId(request.parentId(), projectId)
                    .orElseThrow(() -> new WorkItemNotFoundException("Parent work item not found with id: " + request.parentId()));
        }
        validateHierarchy(type, parent, null);

        Employee assignee = null;
        if (request.assigneeEmpId() != null && !request.assigneeEmpId().isBlank()) {
            projectPermissionService.assertPermission(actor, project, ProjectPermission.ASSIGN_WORK_ITEMS);
            assignee = resolveAndValidateAssignee(request.assigneeEmpId(), project);
        }

        WorkItemPriority priority = request.priority() != null ?
                WorkItemPriority.valueOf(request.priority()) : WorkItemPriority.MEDIUM;

        int nextNumber = workItemRepo.findMaxItemNumberByProjectId(projectId) + 1;
        String itemKey = project.getProjectKey() + "-" + nextNumber;

        WorkItem workItem = new WorkItem();
        workItem.setProject(project);
        workItem.setItemKey(itemKey);
        workItem.setItemNumber(nextNumber);
        workItem.setTitle(request.title());
        workItem.setDescription(request.description());
        workItem.setType(type);
        workItem.setStatus(WorkItemStatus.BACKLOG);
        workItem.setPriority(priority);
        workItem.setEstimationPoints(request.estimationPoints());
        workItem.setParent(parent);
        workItem.setAssignee(assignee);
        workItem.setReporterEmpId(actor.getEmpId());
        workItem.setSprintId(request.sprintId());
        workItem.setAcceptanceCriteria(request.acceptanceCriteria());
        workItem.setStartDate(request.startDate());
        workItem.setDueDate(request.dueDate());

        return WorkItemResponseDTO.fromEntity(workItemRepo.save(workItem));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WorkItemResponseDTO getWorkItem(Long projectId, Long itemId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        WorkItem item = workItemRepo.findByIdAndProjectProjectId(itemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + itemId));

        return WorkItemResponseDTO.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponseDTO> getWorkItems(Long projectId,
                                                  String type,
                                                  String status,
                                                  String priority,
                                                  String assigneeEmpId,
                                                  Long sprintId,
                                                  Long parentId,
                                                  Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        Specification<WorkItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("project").get("projectId"), projectId));

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), WorkItemType.valueOf(type)));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), WorkItemStatus.valueOf(status)));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), WorkItemPriority.valueOf(priority)));
            }
            if (assigneeEmpId != null && !assigneeEmpId.isBlank()) {
                predicates.add(cb.equal(root.get("assignee").get("empId"), assigneeEmpId));
            }
            if (sprintId != null) {
                predicates.add(cb.equal(root.get("sprintId"), sprintId));
            }
            if (parentId != null) {
                predicates.add(cb.equal(root.get("parent").get("id"), parentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return workItemRepo.findAll(spec, pageable).map(WorkItemResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponseDTO> getBacklog(Long projectId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        return workItemRepo.findByProjectProjectIdAndSprintIdIsNull(projectId, pageable)
                .map(WorkItemResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponseDTO> getChildren(Long projectId, Long parentId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        // Verify parent belongs to project
        workItemRepo.findByIdAndProjectProjectId(parentId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Parent work item not found with id: " + parentId));

        return workItemRepo.findByParentId(parentId).stream()
                .map(WorkItemResponseDTO::fromEntity)
                .toList();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public WorkItemResponseDTO updateWorkItem(Long projectId, Long itemId, UpdateWorkItemRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);
        WorkItem item = workItemRepo.findByIdAndProjectProjectId(itemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + itemId));

        // Check ownership vs any update
        boolean isOwner = actor.getEmpId().equalsIgnoreCase(item.getReporterEmpId())
                || (item.getAssignee() != null && actor.getEmpId().equalsIgnoreCase(item.getAssignee().getEmpId()));

        if (isOwner) {
            projectPermissionService.assertPermission(actor, project, ProjectPermission.UPDATE_OWN_WORK_ITEMS);
        } else {
            projectPermissionService.assertPermission(actor, project, ProjectPermission.UPDATE_ANY_WORK_ITEMS);
        }

        if (request.title() != null && !request.title().isBlank()) {
            item.setTitle(request.title());
        }
        if (request.description() != null) {
            item.setDescription(request.description());
        }
        if (request.priority() != null && !request.priority().isBlank()) {
            item.setPriority(WorkItemPriority.valueOf(request.priority()));
        }
        if (request.estimationPoints() != null) {
            item.setEstimationPoints(request.estimationPoints());
        }
        if (request.parentId() != null) {
            WorkItem newParent = workItemRepo.findByIdAndProjectProjectId(request.parentId(), projectId)
                    .orElseThrow(() -> new WorkItemNotFoundException("Parent work item not found with id: " + request.parentId()));
            validateHierarchy(item.getType(), newParent, item.getId());
            item.setParent(newParent);
        }
        if (request.acceptanceCriteria() != null) {
            item.setAcceptanceCriteria(request.acceptanceCriteria());
        }
        if (request.startDate() != null) {
            item.setStartDate(request.startDate());
        }
        if (request.dueDate() != null) {
            item.setDueDate(request.dueDate());
        }

        return WorkItemResponseDTO.fromEntity(workItemRepo.save(item));
    }

    @Transactional
    public WorkItemResponseDTO updateStatus(Long projectId, Long itemId, ChangeWorkItemStatusRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);
        WorkItem item = workItemRepo.findByIdAndProjectProjectId(itemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + itemId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.CHANGE_WORK_ITEM_STATUS);

        WorkItemStatus nextStatus = WorkItemStatus.valueOf(request.newStatus());
        if (!item.getStatus().canTransitionTo(nextStatus)) {
            throw new InvalidWorkItemStatusTransitionException(String.format(
                    "Invalid status transition from '%s' to '%s'", item.getStatus(), nextStatus
            ));
        }

        item.setStatus(nextStatus);
        return WorkItemResponseDTO.fromEntity(workItemRepo.save(item));
    }

    @Transactional
    public WorkItemResponseDTO assignWorkItem(Long projectId, Long itemId, AssignWorkItemRequestDTO request) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = getProjectOrThrow(projectId);
        WorkItem item = workItemRepo.findByIdAndProjectProjectId(itemId, projectId)
                .orElseThrow(() -> new WorkItemNotFoundException("Work item not found with id: " + itemId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.ASSIGN_WORK_ITEMS);

        if (request.assigneeEmpId() == null || request.assigneeEmpId().isBlank()) {
            item.setAssignee(null);
        } else {
            Employee assignee = resolveAndValidateAssignee(request.assigneeEmpId(), project);
            item.setAssignee(assignee);
        }

        return WorkItemResponseDTO.fromEntity(workItemRepo.save(item));
    }

    // ── VALIDATION & HELPERS ──────────────────────────────────────────────────

    private Project getProjectOrThrow(Long projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }

    private Employee resolveAndValidateAssignee(String empId, Project project) {
        Employee assignee = employeeRepo.findByEmpId(empId)
                .orElseThrow(() -> new EmployeeNotFoundException("Assignee employee not found with empId: " + empId));

        if (!"Active".equalsIgnoreCase(assignee.getEmployeeStatus())) {
            throw new IllegalArgumentException("Cannot assign work item to inactive employee: " + empId);
        }

        boolean inScope = employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                assignee.getEmpId(),
                project.getWorkspace().getWId(),
                project.getDeptId()
        ) > 0;

        if (!inScope) {
            throw new IllegalArgumentException(String.format(
                    "Assignee '%s' does not belong to project workspace %d and department '%s'",
                    empId, project.getWorkspace().getWId(), project.getDeptId()
            ));
        }

        return assignee;
    }

    public void validateHierarchy(WorkItemType childType, WorkItem parent, Long currentItemId) {
        if (parent == null) {
            return;
        }

        if (currentItemId != null && currentItemId.equals(parent.getId())) {
            throw new InvalidHierarchyException("A work item cannot be its own parent");
        }

        WorkItemType parentType = parent.getType();

        switch (childType) {
            case MISSION -> throw new InvalidHierarchyException("MISSION is a top-level item and cannot have a parent");
            case CAPABILITY -> {
                if (parentType != WorkItemType.MISSION) {
                    throw new InvalidHierarchyException("CAPABILITY parent must be a MISSION, but got " + parentType);
                }
            }
            case USER_OUTCOME -> {
                if (parentType != WorkItemType.CAPABILITY && parentType != WorkItemType.MISSION) {
                    throw new InvalidHierarchyException("USER_OUTCOME parent must be a CAPABILITY or MISSION, but got " + parentType);
                }
            }
            case WORK_ITEM -> {
                if (parentType != WorkItemType.USER_OUTCOME && parentType != WorkItemType.CAPABILITY && parentType != WorkItemType.MISSION) {
                    throw new InvalidHierarchyException("WORK_ITEM parent must be a USER_OUTCOME, CAPABILITY or MISSION, but got " + parentType);
                }
            }
            case ACTION_STEP -> {
                if (parentType != WorkItemType.WORK_ITEM && parentType != WorkItemType.DEFECT && parentType != WorkItemType.USER_OUTCOME) {
                    throw new InvalidHierarchyException("ACTION_STEP parent must be a WORK_ITEM, DEFECT or USER_OUTCOME, but got " + parentType);
                }
            }
            case DEFECT -> {
                if (parentType != WorkItemType.CAPABILITY && parentType != WorkItemType.USER_OUTCOME && parentType != WorkItemType.WORK_ITEM) {
                    throw new InvalidHierarchyException("DEFECT parent must be a CAPABILITY, USER_OUTCOME or WORK_ITEM, but got " + parentType);
                }
            }
            case DISCOVERY -> {
                if (parentType != WorkItemType.MISSION && parentType != WorkItemType.CAPABILITY) {
                    throw new InvalidHierarchyException("DISCOVERY parent must be a MISSION or CAPABILITY, but got " + parentType);
                }
            }
            case ENHANCEMENT -> {
                if (parentType != WorkItemType.CAPABILITY && parentType != WorkItemType.USER_OUTCOME) {
                    throw new InvalidHierarchyException("ENHANCEMENT parent must be a CAPABILITY or USER_OUTCOME, but got " + parentType);
                }
            }
            case CHECKPOINT -> {
                if (parentType != WorkItemType.MISSION) {
                    throw new InvalidHierarchyException("CHECKPOINT parent must be a MISSION, but got " + parentType);
                }
            }
        }
    }
}
