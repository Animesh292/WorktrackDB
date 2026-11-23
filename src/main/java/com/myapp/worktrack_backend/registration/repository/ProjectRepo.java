package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    /** All projects in a specific department within a workspace — for Admin/HOD scope */
    Page<Project> findByWorkspaceWIdAndDeptId(int workspaceId, String deptId, Pageable pageable);

    /** All projects across a workspace — for Super-Admin workspace-level view */
    Page<Project> findByWorkspaceWId(int workspaceId, Pageable pageable);

    /** Check if a project key already exists in the same workspace+department scope */
    boolean existsByProjectKeyAndWorkspaceWIdAndDeptId(String projectKey, int workspaceId, String deptId);

    /** All projects a specific employee created — useful for dashboards */
    List<Project> findByCreatedByEmpId(String empId);

    /**
     * Projects visible to a staff member — only those in which they are a member.
     * Phase 3 (ProjectMember) will add the join. This query is a placeholder
     * that will be replaced when ProjectMember entity is introduced.
     * For now returns empty — Staff project visibility is gated in service layer.
     */
    @Query("SELECT p FROM Project p WHERE p.workspace.wId = :workspaceId AND p.deptId = :deptId AND p.status != 'CANCELLED'")
    List<Project> findActiveProjectsByWorkspaceAndDept(
            @Param("workspaceId") int workspaceId,
            @Param("deptId") String deptId);

    @Query("SELECT p.status as status, COUNT(p) as count FROM Project p GROUP BY p.status")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countAllGroupedByStatus();

    @Query("SELECT p.status as status, COUNT(p) as count FROM Project p WHERE p.workspace.wId = :wId AND p.deptId = :deptId GROUP BY p.status")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countByDeptGroupedByStatus(@Param("wId") int wId, @Param("deptId") String deptId);

    long countByWorkspaceWIdAndDeptId(int workspaceId, String deptId);
}
