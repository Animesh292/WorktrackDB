-- ============================================================
-- V8__add_active_sprint_constraint.sql
-- Priority 2: Enforce exactly one active sprint per project at DB level.
--
-- Uses a generated column that evaluates to 'ACTIVE' only when state = 'ACTIVE',
-- and NULL for PLANNED, COMPLETED, or CANCELLED sprints.
-- The unique constraint on (project_id, active_sprint_guard) allows unlimited
-- planned/completed/cancelled sprints (since NULL != NULL in SQL unique indexes),
-- but rejects any attempt to insert or update a second ACTIVE sprint.
-- ============================================================

ALTER TABLE sprint
    ADD COLUMN active_sprint_guard VARCHAR(10)
    GENERATED ALWAYS AS (CASE WHEN state = 'ACTIVE' THEN 'ACTIVE' ELSE NULL END);

ALTER TABLE sprint
    ADD CONSTRAINT uq_project_one_active_sprint
    UNIQUE (project_id, active_sprint_guard);
