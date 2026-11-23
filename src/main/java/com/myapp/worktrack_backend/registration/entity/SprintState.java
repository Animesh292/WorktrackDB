package com.myapp.worktrack_backend.registration.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State lifecycle for WorkTrack Sprints (Section 14).
 *
 * Lifecycle:
 *   PLANNED   → ACTIVE, CANCELLED
 *   ACTIVE    → COMPLETED, CANCELLED
 *   COMPLETED → (terminal)
 *   CANCELLED → (terminal)
 */
public enum SprintState {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    private static final Map<SprintState, Set<SprintState>> ALLOWED_TRANSITIONS = Map.of(
            PLANNED, EnumSet.of(ACTIVE, CANCELLED),
            ACTIVE, EnumSet.of(COMPLETED, CANCELLED),
            COMPLETED, EnumSet.noneOf(SprintState.class),
            CANCELLED, EnumSet.noneOf(SprintState.class)
    );

    public boolean canTransitionTo(SprintState next) {
        if (this == next) {
            return true;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }
}
