package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project entity — core domain object for WorkTrack V1.
 *
 * A Project belongs to exactly ONE Workspace and ONE Department.
 * Cross-department projects are out of V1 scope.
 *
 * Status lifecycle:
 *   DRAFT → ACTIVE → COMPLETED
 *                 ↘ CANCELLED
 *   DRAFT → CANCELLED
 *
 * projectKey is unique within (workspace, department) scope.
 * Uniqueness is enforced both by DB unique constraint and service validation.
 */
@Getter
@Setter
@Entity
@Table(
    name = "project",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_project_key_workspace_dept",
            columnNames = {"project_key", "workspace_id", "dept_id"}
        )
    }
)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    /**
     * Human-readable project key (e.g. "WT-001", "PAYROLL-Q1").
     * Unique within the workspace + department combination.
     */
    @Column(name = "project_key", nullable = false, length = 50)
    private String projectKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The workspace this project belongs to.
     * Stored as FK workspace_id referencing workspace.w_id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /**
     * Department business ID (e.g. "D001").
     * Together with workspace_id forms the department composite key.
     * Stored as a plain column — no FK join to department composite PK
     * to keep the model simple; referential integrity maintained at service level.
     */
    @Column(name = "dept_id", nullable = false, length = 5)
    private String deptId;

    /**
     * Business empId (e.g. "EM000003") of the employee who created this project.
     * Not a FK — history must survive employee termination.
     */
    @Column(name = "created_by_emp_id", nullable = false, length = 50)
    private String createdByEmpId;

    /**
     * Project lifecycle status.
     * Valid values: DRAFT, ACTIVE, COMPLETED, CANCELLED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "DRAFT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
