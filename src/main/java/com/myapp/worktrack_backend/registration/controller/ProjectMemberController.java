package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.AddProjectMemberRequestDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectMemberResponseDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateMemberRoleRequestDTO;
import com.myapp.worktrack_backend.registration.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Project Membership management.
 *
 * Endpoints:
 *   POST   /projects/{projectId}/members              — add member
 *   GET    /projects/{projectId}/members              — list members (paginated)
 *   GET    /projects/{projectId}/members/me           — get current user's membership
 *   PUT    /projects/{projectId}/members/{memberId}/role — change role
 *   DELETE /projects/{projectId}/members/{memberId}   — soft remove member
 */
@RestController
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponseDTO> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddProjectMemberRequestDTO request) {
        ProjectMemberResponseDTO response = projectMemberService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectMemberResponseDTO>> getMembers(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProjectMemberResponseDTO> response = projectMemberService.getProjectMembers(projectId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProjectMemberResponseDTO> getMyMembership(@PathVariable Long projectId) {
        ProjectMemberResponseDTO response = projectMemberService.getMyMembership(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<ProjectMemberResponseDTO> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequestDTO request) {
        ProjectMemberResponseDTO response = projectMemberService.updateMemberRole(projectId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ProjectMemberResponseDTO> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        ProjectMemberResponseDTO response = projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.ok(response);
    }
}
