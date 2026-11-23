package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.CommentResponseDTO;
import com.myapp.worktrack_backend.registration.dto.CreateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateCommentRequestDTO;
import com.myapp.worktrack_backend.registration.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Work-Item Comments (Section 15).
 */
@RestController
@RequestMapping("/projects/{projectId}/work-items/{itemId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @Valid @RequestBody CreateCommentRequestDTO request) {
        CommentResponseDTO response = commentService.addComment(projectId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponseDTO>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentResponseDTO> response = commentService.getComments(projectId, itemId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequestDTO request) {
        CommentResponseDTO response = commentService.updateComment(projectId, itemId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @PathVariable Long commentId) {
        commentService.deleteComment(projectId, itemId, commentId);
        return ResponseEntity.noContent().build();
    }
}
