package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.DepartmentResponseDTO;
import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.DepartmentWorkspace;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import com.myapp.worktrack_backend.registration.exceptionhandler.*;
import com.myapp.worktrack_backend.registration.repository.DepartmentRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.WorkspaceRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Super-Admin department management operations:
 * - Assigning / clearing a Department Head (HOD)
 *
 * HOD eligibility rules (spec section 5):
 *   - Must be an existing, Active employee
 *   - Must have the Admin system role
 *   - Must be assigned to the same workspace as the department
 *   - Appointment is department-specific
 *
 * Only a Super-Admin (temporary or permanent) may assign the HOD.
 */
@Service
public class DepartmentAdminService {

    private final DepartmentRepo departmentRepo;
    private final WorkspaceRepo workspaceRepo;
    private final EmployeeRepo employeeRepo;
    private final EmployeeWorkspaceRepo employeeWorkspaceRepo;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public DepartmentAdminService(DepartmentRepo departmentRepo,
                                  WorkspaceRepo workspaceRepo,
                                  EmployeeRepo employeeRepo,
                                  EmployeeWorkspaceRepo employeeWorkspaceRepo,
                                  AuthenticatedEmployeeDetails authEmpDetails) {
        this.departmentRepo = departmentRepo;
        this.workspaceRepo = workspaceRepo;
        this.employeeRepo = employeeRepo;
        this.employeeWorkspaceRepo = employeeWorkspaceRepo;
        this.authEmpDetails = authEmpDetails;
    }

    /**
     * Assigns an eligible employee as Department Head for the given department.
     *
     * @param workspaceId workspace of the department
     * @param deptId      department string ID (e.g. "D001")
     * @param hodEmpId    business empId of the employee to appoint as HOD
     */
    @Transactional
    public DepartmentResponseDTO assignDepartmentHead(int workspaceId, String deptId, String hodEmpId) {
        // 1. Verify actor is an active Super-Admin
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        if (!"Super-Admin".equals(actor.getRole())) {
            throw new AccessDeniedException("Only a Super-Admin can assign a Department Head");
        }

        // 2. Resolve workspace
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFound("Workspace not found"));
        if (!"Active".equalsIgnoreCase(workspace.getStatus())) {
            throw new WorkspaceIsInactiveException("Workspace is inactive");
        }

        // 3. Resolve department (with write lock to prevent concurrent modification)
        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(workspaceId);
        dw.setDeptId(deptId);
        Department department = departmentRepo.findByIdForUpdate(dw)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        // 4. Resolve the HOD candidate
        Employee hodCandidate = employeeRepo.findByEmpId(hodEmpId)
                .orElseThrow(() -> new EmployeeNotFoundException("Candidate employee not found: " + hodEmpId));

        // 5. Validate HOD eligibility
        validateHodEligibility(hodCandidate, workspaceId);

        // 6. Assign and persist
        department.setDepartmentHead(hodCandidate);
        departmentRepo.save(department);

        return toDepartmentResponseDTO(department);
    }

    /**
     * Clears the Department Head from a department.
     * Only a Super-Admin may perform this action.
     */
    @Transactional
    public DepartmentResponseDTO clearDepartmentHead(int workspaceId, String deptId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        if (!"Super-Admin".equals(actor.getRole())) {
            throw new AccessDeniedException("Only a Super-Admin can clear a Department Head");
        }

        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFound("Workspace not found"));

        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(workspaceId);
        dw.setDeptId(deptId);
        Department department = departmentRepo.findByIdForUpdate(dw)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        department.setDepartmentHead(null);
        departmentRepo.save(department);
        return toDepartmentResponseDTO(department);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void validateHodEligibility(Employee candidate, int workspaceId) {
        // Must be Active
        if (!"Active".equals(candidate.getEmployeeStatus())) {
            throw new DepartmentHeadInEligible(
                    "HOD candidate must be Active. Current status: " + candidate.getEmployeeStatus());
        }
        // Must have Admin role
        if (!"Admin".equals(candidate.getRole())) {
            throw new DepartmentHeadInEligible(
                    "HOD candidate must have Admin role. Current role: " + candidate.getRole());
        }
        // Must be assigned to the same workspace
        long workspaceAssignmentCount = employeeWorkspaceRepo
                .isAssignedToWorkspace(candidate.getEmpId(), workspaceId);
        if (workspaceAssignmentCount == 0) {
            throw new DepartmentHeadInEligible(
                    "HOD candidate is not assigned to workspace " + workspaceId);
        }
    }

    private DepartmentResponseDTO toDepartmentResponseDTO(Department dept) {
        Employee head = dept.getDepartmentHead();
        String headEmpId = head != null ? head.getEmpId() : null;
        return new DepartmentResponseDTO(
                dept.getDepartmentwkspc().getDeptId(),
                dept.getDepartmentwkspc().getWId(),
                dept.getDeptName(),
                dept.getStatus(),
                dept.getCapacity(),
                dept.getBudget(),
                headEmpId,
                null  // name lookup not needed here; keep response lean
        );
    }
}
