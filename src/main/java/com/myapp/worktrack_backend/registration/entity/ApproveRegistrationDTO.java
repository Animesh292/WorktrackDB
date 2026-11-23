package com.myapp.worktrack_backend.registration.entity;

import lombok.Data;

@Data
public class ApproveRegistrationDTO {
 private String role;
 private int workspaceId;
 private String departmentId;
}
