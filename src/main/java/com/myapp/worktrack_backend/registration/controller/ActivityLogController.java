package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.ActivityLogResponseDTO;
import com.myapp.worktrack_backend.registration.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Activity / Audit Trail history (Section 16).
 */
@RestController
@RequestMapping("/projects/{projectId}/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<Page<ActivityLogResponseDTO>> getProjectActivity(
            @PathVariable Long projectId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<ActivityLogResponseDTO> response = activityLogService.getProjectActivity(projectId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Page<ActivityLogResponseDTO>> getEntityActivity(
            @PathVariable Long projectId,
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<ActivityLogResponseDTO> response = activityLogService.getEntityActivity(projectId, entityType, entityId, pageable);
        return ResponseEntity.ok(response);
    }
}
