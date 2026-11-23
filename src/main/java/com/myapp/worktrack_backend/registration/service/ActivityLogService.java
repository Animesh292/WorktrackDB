package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.ActivityLogResponseDTO;
import com.myapp.worktrack_backend.registration.entity.ActivityLog;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Project;
import com.myapp.worktrack_backend.registration.entity.ProjectPermission;
import com.myapp.worktrack_backend.registration.event.ProjectActivityEvent;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.repository.ActivityLogRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepo activityLogRepo;
    private final ProjectRepo projectRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public ActivityLogService(ActivityLogRepo activityLogRepo,
                              ProjectRepo projectRepo,
                              ProjectPermissionService projectPermissionService,
                              AuthenticatedEmployeeDetails authEmpDetails) {
        this.activityLogRepo = activityLogRepo;
        this.projectRepo = projectRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleActivityEvent(ProjectActivityEvent event) {
        if (event == null || event.projectId() == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setProjectId(event.projectId());
        log.setEntityType(event.entityType());
        log.setEntityId(event.entityId());
        log.setAction(event.action());
        log.setActorEmpId(event.actorEmpId());
        log.setDescription(event.description());
        log.setOldValue(event.oldValue());
        log.setNewValue(event.newValue());

        activityLogRepo.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDTO> getProjectActivity(Long projectId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        return activityLogRepo.findByProjectIdOrderByTimestampDesc(projectId, pageable)
                .map(ActivityLogResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDTO> getEntityActivity(Long projectId, String entityType, Long entityId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        return activityLogRepo.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
                .map(ActivityLogResponseDTO::fromEntity);
    }
}
