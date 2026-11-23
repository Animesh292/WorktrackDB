-- ============================================================
-- V2__add_project_table.sql
-- Adds the project table for WorkTrack Phase 2.
-- ============================================================

CREATE TABLE IF NOT EXISTS project (
    project_id              BIGINT          NOT NULL AUTO_INCREMENT,
    project_key             VARCHAR(50)     NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    description             TEXT,
    workspace_id            INT             NOT NULL,
    dept_id                 VARCHAR(5)      NOT NULL,
    created_by_emp_id       VARCHAR(50)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    start_date              DATE,
    target_end_date         DATE,
    actual_completion_date  DATE,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,
    PRIMARY KEY (project_id),
    CONSTRAINT uq_project_key_workspace_dept
        UNIQUE (project_key, workspace_id, dept_id),
    CONSTRAINT fk_project_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (w_id)
);

CREATE INDEX IF NOT EXISTS idx_project_workspace_dept
    ON project (workspace_id, dept_id);

CREATE INDEX IF NOT EXISTS idx_project_status
    ON project (status);

CREATE INDEX IF NOT EXISTS idx_project_created_by
    ON project (created_by_emp_id);
