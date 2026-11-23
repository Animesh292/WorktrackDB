package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.AddProjectMemberRequestDTO;
import com.myapp.worktrack_backend.registration.dto.ProjectMemberResponseDTO;
import com.myapp.worktrack_backend.registration.dto.UpdateMemberRoleRequestDTO;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Project;
import com.myapp.worktrack_backend.registration.entity.ProjectMember;
import com.myapp.worktrack_backend.registration.entity.ProjectMemberRole;
import com.myapp.worktrack_backend.registration.entity.ProjectPermission;
import com.myapp.worktrack_backend.registration.exceptionhandler.DuplicateMembershipException;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectMemberNotFoundException;
import com.myapp.worktrack_backend.registration.exceptionhandler.ProjectNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectMemberRepo;
import com.myapp.worktrack_backend.registration.repository.ProjectRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectRepo projectRepo;
    private final EmployeeRepo employeeRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;
    private final ProjectPermissionService projectPermissionService;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public ProjectMemberService(ProjectMemberRepo projectMemberRepo,
                                ProjectRepo projectRepo,
                                EmployeeRepo employeeRepo,
                                EmployeeWorkspaceRepo employeeWorkspaceRepo,
                                ProjectPermissionService projectPermissionService,
                                AuthenticatedEmployeeDetails authEmpDetails) {
        this.projectMemberRepo = projectMemberRepo;
        this.projectRepo = projectRepo;
        this.employeeRepo = employeeRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
        this.projectPermissionService = projectPermissionService;
        this.authEmpDetails = authEmpDetails;
    }

    @Transactional
    public ProjectMemberResponseDTO addMember(Long projectId, AddProjectMemberRequestDTO dto) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        // Check permission: actor must have MANAGE_MEMBERS on this project
        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_MEMBERS);

        // Look up target employee
        Employee targetEmployee = employeeRepo.findByEmpId(dto.empId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with empId: " + dto.empId()));

        // Validate target is Active
        if (!"Active".equalsIgnoreCase(targetEmployee.getEmployeeStatus())) {
            throw new IllegalArgumentException("Only active employees can be added to projects. Target status: " + targetEmployee.getEmployeeStatus());
        }

        // Validate target belongs to the same workspace and department as the project
        boolean inSameScope = employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(
                targetEmployee.getEmpId(),
                project.getWorkspace().getWId(),
                project.getDeptId()
        ) > 0;
        if (!inSameScope) {
            throw new IllegalArgumentException(String.format(
                    "Employee '%s' does not belong to workspace %d and department '%s'",
                    targetEmployee.getEmpId(), project.getWorkspace().getWId(), project.getDeptId()
            ));
        }

        ProjectMemberRole newRole = ProjectMemberRole.valueOf(dto.role());

        // Check for existing membership
        Optional<ProjectMember> existingOpt = projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeId(
                projectId, targetEmployee.getEmployeeId()
        );

        ProjectMember member;
        if (existingOpt.isPresent()) {
            member = existingOpt.get();
            if ("ACTIVE".equalsIgnoreCase(member.getStatus())) {
                throw new DuplicateMembershipException(String.format(
                        "Employee '%s' is already an active member of project '%s'",
                        targetEmployee.getEmpId(), project.getName()
                ));
            }
            // Reactivate previously removed member
            member.setStatus("ACTIVE");
            member.setRole(newRole);
            member.setAddedByEmpId(actor.getEmpId());
        } else {
            member = new ProjectMember();
            member.setProject(project);
            member.setEmployee(targetEmployee);
            member.setRole(newRole);
            member.setStatus("ACTIVE");
            member.setAddedByEmpId(actor.getEmpId());
        }

        ProjectMember saved = projectMemberRepo.save(member);
        return ProjectMemberResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ProjectMemberResponseDTO removeMember(Long projectId, Long memberId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_MEMBERS);

        ProjectMember member = projectMemberRepo.findByIdAndProjectProjectId(memberId, projectId)
                .orElseThrow(() -> new ProjectMemberNotFoundException("Project member not found with id: " + memberId + " in project: " + projectId));

        member.setStatus("REMOVED");
        ProjectMember saved = projectMemberRepo.save(member);
        return ProjectMemberResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ProjectMemberResponseDTO updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequestDTO dto) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.MANAGE_MEMBERS);

        ProjectMember member = projectMemberRepo.findByIdAndProjectProjectId(memberId, projectId)
                .orElseThrow(() -> new ProjectMemberNotFoundException("Project member not found with id: " + memberId + " in project: " + projectId));

        if ("REMOVED".equalsIgnoreCase(member.getStatus())) {
            throw new IllegalArgumentException("Cannot update role of a removed member. Please re-add them instead.");
        }

        member.setRole(ProjectMemberRole.valueOf(dto.role()));
        ProjectMember saved = projectMemberRepo.save(member);
        return ProjectMemberResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectMemberResponseDTO> getProjectMembers(Long projectId, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        projectPermissionService.assertPermission(actor, project, ProjectPermission.VIEW_PROJECT);

        return projectMemberRepo.findByProjectProjectIdAndStatus(projectId, "ACTIVE", pageable)
                .map(ProjectMemberResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProjectMemberResponseDTO getMyMembership(Long projectId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        return projectMemberRepo.findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(
                projectId, actor.getEmployeeId(), "ACTIVE"
        ).map(ProjectMemberResponseDTO::fromEntity)
        .orElseThrow(() -> new ProjectMemberNotFoundException("You are not an active member of project ID: " + projectId));
    }
}
