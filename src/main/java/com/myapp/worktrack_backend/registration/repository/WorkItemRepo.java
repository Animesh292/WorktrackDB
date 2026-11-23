package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.WorkItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemRepo extends JpaRepository<WorkItem, Long>, JpaSpecificationExecutor<WorkItem> {

    Optional<WorkItem> findByIdAndProjectProjectId(Long id, Long projectId);

    Optional<WorkItem> findByProjectProjectIdAndItemKey(Long projectId, String itemKey);

    Page<WorkItem> findByProjectProjectId(Long projectId, Pageable pageable);

    Page<WorkItem> findByProjectProjectIdAndSprintIdIsNull(Long projectId, Pageable pageable);

    Page<WorkItem> findByProjectProjectIdAndSprintId(Long projectId, Long sprintId, Pageable pageable);

    List<WorkItem> findByProjectProjectIdAndSprintId(Long projectId, Long sprintId);

    List<WorkItem> findByParentId(Long parentId);

    @Query("SELECT coalesce(max(w.itemNumber), 0) FROM WorkItem w WHERE w.project.projectId = :projectId")
    int findMaxItemNumberByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT w.status as status, COUNT(w) as count FROM WorkItem w WHERE w.project.projectId = :projectId GROUP BY w.status")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countByProjectGroupedByStatus(@Param("projectId") Long projectId);

    @Query("SELECT w.type as type, COUNT(w) as count FROM WorkItem w WHERE w.project.projectId = :projectId GROUP BY w.type")
    List<com.myapp.worktrack_backend.registration.repository.projection.TypeCountProjection> countByProjectGroupedByType(@Param("projectId") Long projectId);

    @Query("SELECT w.status as status, COUNT(w) as count FROM WorkItem w WHERE w.project.workspace.wId = :wId AND w.project.deptId = :deptId GROUP BY w.status")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countByDepartmentGroupedByStatus(@Param("wId") int wId, @Param("deptId") String deptId);

    @Query("SELECT w.assignee.empId as assigneeEmpId, COUNT(w) as count FROM WorkItem w WHERE w.project.workspace.wId = :wId AND w.project.deptId = :deptId AND w.assignee IS NOT NULL GROUP BY w.assignee.empId")
    List<com.myapp.worktrack_backend.registration.repository.projection.AssigneeCountProjection> countWorkloadByDepartment(@Param("wId") int wId, @Param("deptId") String deptId);

    @Query("SELECT w.status as status, COUNT(w) as count FROM WorkItem w WHERE w.assignee.empId = :empId GROUP BY w.status")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countByAssigneeGroupedByStatus(@Param("empId") String empId);

    long countByReporterEmpId(String reporterEmpId);

    @Query("SELECT coalesce(SUM(w.estimationPoints), 0) FROM WorkItem w WHERE w.project.projectId = :projectId")
    Long sumEstimationPointsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT coalesce(SUM(w.estimationPoints), 0) FROM WorkItem w WHERE w.project.projectId = :projectId AND w.status = 'COMPLETED'")
    Long sumCompletedEstimationPointsByProjectId(@Param("projectId") Long projectId);

    long countByDueDateBeforeAndStatusNotIn(java.time.LocalDate date, List<com.myapp.worktrack_backend.registration.entity.WorkItemStatus> finishedStatuses);

    long countByStatus(com.myapp.worktrack_backend.registration.entity.WorkItemStatus status);

    long countByProjectWorkspaceWIdAndProjectDeptIdAndStatus(int wId, String deptId, com.myapp.worktrack_backend.registration.entity.WorkItemStatus status);

    long countByProjectWorkspaceWIdAndProjectDeptIdAndDueDateBeforeAndStatusNotIn(int wId, String deptId, java.time.LocalDate date, List<com.myapp.worktrack_backend.registration.entity.WorkItemStatus> finishedStatuses);

    long countByAssigneeEmpIdAndDueDateBeforeAndStatusNotIn(String empId, java.time.LocalDate date, List<com.myapp.worktrack_backend.registration.entity.WorkItemStatus> finishedStatuses);

    long countByAssigneeEmpIdAndStatus(String empId, com.myapp.worktrack_backend.registration.entity.WorkItemStatus status);
}
