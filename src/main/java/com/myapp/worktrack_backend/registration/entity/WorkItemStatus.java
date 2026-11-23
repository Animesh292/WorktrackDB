package com.myapp.worktrack_backend.registration.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Status workflow for WorkTrack Work Items (Section 13).
 *
 * Workflow:
 *   BACKLOG      → READY, IN_PROGRESS, CANCELLED
 *   READY        → IN_PROGRESS, BACKLOG, CANCELLED
 *   IN_PROGRESS  → IN_REVIEW, BLOCKED, COMPLETED, BACKLOG, CANCELLED
 *   IN_REVIEW    → IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED
 *   BLOCKED      → IN_PROGRESS, READY, CANCELLED
 *   COMPLETED    → IN_PROGRESS (reopened), CANCELLED
 *   CANCELLED    → BACKLOG (reopened)
 */
public enum WorkItemStatus {
    BACKLOG,
    READY,
    IN_PROGRESS,
    IN_REVIEW,
    BLOCKED,
    COMPLETED,
    CANCELLED;

    private static final Map<WorkItemStatus, Set<WorkItemStatus>> VALID_TRANSITIONS = Map.of(
            BACKLOG, EnumSet.of(READY, IN_PROGRESS, CANCELLED),
            READY, EnumSet.of(IN_PROGRESS, BACKLOG, CANCELLED),
            IN_PROGRESS, EnumSet.of(IN_REVIEW, BLOCKED, COMPLETED, BACKLOG, CANCELLED),
            IN_REVIEW, EnumSet.of(IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED),
            BLOCKED, EnumSet.of(IN_PROGRESS, READY, CANCELLED),
            COMPLETED, EnumSet.of(IN_PROGRESS, CANCELLED),
            CANCELLED, EnumSet.of(BACKLOG)
    );

    public boolean canTransitionTo(WorkItemStatus next) {
        if (this == next) {
            return true;
        }
        Set<WorkItemStatus> allowed = VALID_TRANSITIONS.getOrDefault(this, Set.of());
        return allowed.contains(next);
    }
}
