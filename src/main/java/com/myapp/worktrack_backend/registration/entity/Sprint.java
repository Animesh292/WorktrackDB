package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sprint entity for WorkTrack iteration management (Section 14).
 *
 * Invariant: Exactly one ACTIVE sprint per project at any given time.
 */
@Getter
@Setter
@Entity
@Table(
    name = "sprints",
    indexes = {
        @Index(name = "idx_sprints_project", columnList = "project_id"),
        @Index(name = "idx_sprints_state", columnList = "state")
    }
)
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 30)
    private SprintState state = SprintState.PLANNED;

    @Column(name = "created_by_emp_id", nullable = false, length = 50)
    private String createdByEmpId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.state == null) {
            this.state = SprintState.PLANNED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
