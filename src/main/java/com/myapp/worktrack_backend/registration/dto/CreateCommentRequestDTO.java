package com.myapp.worktrack_backend.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequestDTO(
        @NotBlank(message = "Comment content must not be blank")
        @Size(max = 5000, message = "Comment must not exceed 5000 characters")
        String content
) {}
