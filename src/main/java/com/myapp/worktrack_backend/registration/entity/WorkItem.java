package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Generic extensible WorkItem entity for WorkTrack (Section 12).
 * Covers: Mission, Capability, User Outcome, Work Item, Action Step,
 * Defect, Discovery, Enhancement, Checkpoint.
 */
@Getter
@Setter
@Entity
@Table(
    name = "work_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_project_item_key",
            columnNames = {"project_id", "item_key"}
        ),
        @UniqueConstraint(
            name = "uq_project_item_number",
            columnNames = {"project_id", "item_number"}
        )
    }
)
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "item_key", nullable = false, length = 60)
    private String itemKey;

    @Column(name = "item_number", nullable = false)
    private Integer itemNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private WorkItemType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WorkItemStatus status = WorkItemStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private WorkItemPriority priority = WorkItemPriority.MEDIUM;

    @Column(name = "estimation_points")
    private Integer estimationPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private WorkItem parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Employee assignee;

    @Column(name = "reporter_emp_id", nullable = false, length = 50)
    private String reporterEmpId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "acceptance_criteria", columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = WorkItemStatus.BACKLOG;
        }
        if (this.priority == null) {
            this.priority = WorkItemPriority.MEDIUM;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
