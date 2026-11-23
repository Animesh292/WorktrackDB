-- ============================================================
-- V3__add_project_member_table.sql
-- Adds the project_members table for WorkTrack Phase 3.
-- ============================================================

CREATE TABLE IF NOT EXISTS project_members (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    project_id          BIGINT          NOT NULL,
    employee_id         BIGINT          NOT NULL,
    role                VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    added_by_emp_id     VARCHAR(50)     NOT NULL,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_project_employee
        UNIQUE (project_id, employee_id),
    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project_id) REFERENCES project (project_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_member_employee
        FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
);

CREATE INDEX IF NOT EXISTS idx_project_member_project
    ON project_members (project_id);

CREATE INDEX IF NOT EXISTS idx_project_member_employee
    ON project_members (employee_id);

CREATE INDEX IF NOT EXISTS idx_project_member_status
    ON project_members (status);
