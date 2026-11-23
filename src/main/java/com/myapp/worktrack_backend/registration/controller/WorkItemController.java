package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.service.WorkItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Work Items, Backlog, and Task Management (Section 12 & 13).
 */
@RestController
@RequestMapping("/projects/{projectId}")
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @PostMapping("/work-items")
    public ResponseEntity<WorkItemResponseDTO> createWorkItem(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateWorkItemRequestDTO request) {
        WorkItemResponseDTO response = workItemService.createWorkItem(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/work-items/{itemId}")
    public ResponseEntity<WorkItemResponseDTO> getWorkItem(
            @PathVariable Long projectId,
            @PathVariable Long itemId) {
        WorkItemResponseDTO response = workItemService.getWorkItem(projectId, itemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/work-items")
    public ResponseEntity<Page<WorkItemResponseDTO>> getWorkItems(
            @PathVariable Long projectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assigneeEmpId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(required = false) Long parentId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WorkItemResponseDTO> response = workItemService.getWorkItems(
                projectId, type, status, priority, assigneeEmpId, sprintId, parentId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/backlog")
    public ResponseEntity<Page<WorkItemResponseDTO>> getBacklog(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WorkItemResponseDTO> response = workItemService.getBacklog(projectId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/work-items/{itemId}/children")
    public ResponseEntity<List<WorkItemResponseDTO>> getChildren(
            @PathVariable Long projectId,
            @PathVariable Long itemId) {
        List<WorkItemResponseDTO> response = workItemService.getChildren(projectId, itemId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/work-items/{itemId}")
    public ResponseEntity<WorkItemResponseDTO> updateWorkItem(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateWorkItemRequestDTO request) {
        WorkItemResponseDTO response = workItemService.updateWorkItem(projectId, itemId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/work-items/{itemId}/status")
    public ResponseEntity<WorkItemResponseDTO> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @Valid @RequestBody ChangeWorkItemStatusRequestDTO request) {
        WorkItemResponseDTO response = workItemService.updateStatus(projectId, itemId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/work-items/{itemId}/assignee")
    public ResponseEntity<WorkItemResponseDTO> assignWorkItem(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @RequestBody AssignWorkItemRequestDTO request) {
        WorkItemResponseDTO response = workItemService.assignWorkItem(projectId, itemId, request);
        return ResponseEntity.ok(response);
    }
}
