package com.myapp.worktrack_backend.registration.controller;

import com.myapp.worktrack_backend.registration.dto.DepartmentDashboardDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectMetricsDTO;
import com.myapp.worktrack_backend.registration.dto.StaffDashboardDTO;
import com.myapp.worktrack_backend.registration.dto.SuperAdminDashboardDTO;
import com.myapp.worktrack_backend.registration.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Role-Scoped Dashboards and Analytics (Section 16).
 */
@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboards/super-admin")
    public ResponseEntity<SuperAdminDashboardDTO> getSuperAdminDashboard() {
        SuperAdminDashboardDTO response = dashboardService.getSuperAdminDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboards/department/{workspaceId}/{deptId}")
    public ResponseEntity<DepartmentDashboardDTO> getDepartmentDashboard(
            @PathVariable int workspaceId,
            @PathVariable String deptId) {
        DepartmentDashboardDTO response = dashboardService.getDepartmentDashboard(workspaceId, deptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboards/staff")
    public ResponseEntity<StaffDashboardDTO> getStaffDashboard() {
        StaffDashboardDTO response = dashboardService.getStaffDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/metrics")
    public ResponseEntity<ProjectMetricsDTO> getProjectMetrics(@PathVariable Long projectId) {
        ProjectMetricsDTO response = dashboardService.getProjectMetrics(projectId);
        return ResponseEntity.ok(response);
    }
}
