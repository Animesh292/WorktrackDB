-- ============================================================
-- V4__add_work_item_table.sql
-- Adds work_items table for WorkTrack Phase 4 (Sections 12 & 13).
-- ============================================================

CREATE TABLE IF NOT EXISTS work_items (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    project_id          BIGINT          NOT NULL,
    item_key            VARCHAR(60)     NOT NULL,
    item_number         INT             NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT,
    type                VARCHAR(30)     NOT NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'BACKLOG',
    priority            VARCHAR(30)     NOT NULL DEFAULT 'MEDIUM',
    estimation_points   INT,
    parent_id           BIGINT,
    assignee_id         BIGINT,
    reporter_emp_id     VARCHAR(50)     NOT NULL,
    sprint_id           BIGINT,
    acceptance_criteria TEXT,
    start_date          DATE,
    due_date            DATE,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_project_item_key
        UNIQUE (project_id, item_key),
    CONSTRAINT uq_project_item_number
        UNIQUE (project_id, item_number),
    CONSTRAINT fk_work_item_project
        FOREIGN KEY (project_id) REFERENCES project (project_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_work_item_parent
        FOREIGN KEY (parent_id) REFERENCES work_items (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_work_item_assignee
        FOREIGN KEY (assignee_id) REFERENCES employee (employee_id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_work_items_project
    ON work_items (project_id);

CREATE INDEX IF NOT EXISTS idx_work_items_status
    ON work_items (status);

CREATE INDEX IF NOT EXISTS idx_work_items_type
    ON work_items (type);

CREATE INDEX IF NOT EXISTS idx_work_items_priority
    ON work_items (priority);

CREATE INDEX IF NOT EXISTS idx_work_items_assignee
    ON work_items (assignee_id);

CREATE INDEX IF NOT EXISTS idx_work_items_parent
    ON work_items (parent_id);

CREATE INDEX IF NOT EXISTS idx_work_items_sprint
    ON work_items (sprint_id);
