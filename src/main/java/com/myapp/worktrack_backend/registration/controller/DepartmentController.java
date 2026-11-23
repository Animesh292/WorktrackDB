package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.ActiveDepartmentsWorkspaceDTO;
import com.myapp.worktrack_backend.registration.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workspace")
public class DepartmentController {
    private final DepartmentService deptService;

    public DepartmentController(DepartmentService deptService) {
        this.deptService=deptService;
    }

    @GetMapping("/{workspaceId}/departments")
    public ResponseEntity<List<ActiveDepartmentsWorkspaceDTO>> getActiveDepartments(@PathVariable int workspaceId) {
        return ResponseEntity.ok(deptService.getActiveDepartmentsbyWorkspace(workspaceId));
    }
}
