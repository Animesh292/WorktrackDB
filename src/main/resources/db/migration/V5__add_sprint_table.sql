-- ============================================================
-- V5__add_sprint_table.sql
-- Adds sprints table and foreign key for WorkTrack Phase 5 (Section 14).
-- ============================================================

CREATE TABLE IF NOT EXISTS sprints (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    project_id          BIGINT          NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    goal                TEXT,
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    state               VARCHAR(30)     NOT NULL DEFAULT 'PLANNED',
    created_by_emp_id   VARCHAR(50)     NOT NULL,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sprint_project
        FOREIGN KEY (project_id) REFERENCES project (project_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sprints_project
    ON sprints (project_id);

CREATE INDEX IF NOT EXISTS idx_sprints_state
    ON sprints (state);

ALTER TABLE work_items
    ADD CONSTRAINT fk_work_item_sprint
    FOREIGN KEY (sprint_id) REFERENCES sprints (id)
    ON DELETE SET NULL;
