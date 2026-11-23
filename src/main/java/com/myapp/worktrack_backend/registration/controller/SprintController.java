package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.*;
import com.myapp.worktrack_backend.registration.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Sprint Management and Iteration Workflow (Section 14).
 */
@RestController
@RequestMapping("/projects/{projectId}/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<SprintResponseDTO> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSprintRequestDTO request) {
        SprintResponseDTO response = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintResponseDTO> getSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId) {
        SprintResponseDTO response = sprintService.getSprint(projectId, sprintId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<SprintResponseDTO> getActiveSprint(@PathVariable Long projectId) {
        SprintResponseDTO response = sprintService.getActiveSprint(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SprintResponseDTO>> getSprints(
            @PathVariable Long projectId,
            @RequestParam(required = false) String state,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SprintResponseDTO> response = sprintService.getSprints(projectId, state, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintResponseDTO> updateSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @Valid @RequestBody UpdateSprintRequestDTO request) {
        SprintResponseDTO response = sprintService.updateSprint(projectId, sprintId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/start")
    public ResponseEntity<SprintResponseDTO> startSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId) {
        SprintResponseDTO response = sprintService.startSprint(projectId, sprintId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/complete")
    public ResponseEntity<SprintResponseDTO> completeSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @RequestBody(required = false) CompleteSprintRequestDTO request) {
        SprintResponseDTO response = sprintService.completeSprint(projectId, sprintId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/cancel")
    public ResponseEntity<SprintResponseDTO> cancelSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId) {
        SprintResponseDTO response = sprintService.cancelSprint(projectId, sprintId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sprintId}/work-items")
    public ResponseEntity<Page<WorkItemResponseDTO>> getSprintWorkItems(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WorkItemResponseDTO> response = sprintService.getSprintWorkItems(projectId, sprintId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/work-items")
    public ResponseEntity<List<WorkItemResponseDTO>> addWorkItemsToSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @Valid @RequestBody AddWorkItemsToSprintRequestDTO request) {
        List<WorkItemResponseDTO> response = sprintService.addWorkItemsToSprint(projectId, sprintId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sprintId}/work-items/{itemId}")
    public ResponseEntity<WorkItemResponseDTO> removeWorkItemFromSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PathVariable Long itemId) {
        WorkItemResponseDTO response = sprintService.removeWorkItemFromSprint(projectId, sprintId, itemId);
        return ResponseEntity.ok(response);
    }
}
