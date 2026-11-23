package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.ActiveWorkspaceResponseDTO;
import com.myapp.worktrack_backend.registration.service.WorkspaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService=workspaceService;
    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveWorkspaces() {
        List<ActiveWorkspaceResponseDTO> activeWorkspacesDTO = workspaceService.getActiveWorkspaces();
        return new ResponseEntity<>(activeWorkspacesDTO, HttpStatus.OK);
    }
}
