-- ============================================================
-- V6__add_collaboration_tables.sql
-- Adds tables for WorkTrack Phase 6: comments, activity audit, notifications.
-- ============================================================

CREATE TABLE IF NOT EXISTS work_item_comments (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    work_item_id    BIGINT          NOT NULL,
    author_id       BIGINT          NOT NULL,
    content         TEXT            NOT NULL,
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comment_author
        FOREIGN KEY (author_id) REFERENCES employee (employee_id)
);

CREATE INDEX IF NOT EXISTS idx_comment_work_item
    ON work_item_comments (work_item_id);

CREATE INDEX IF NOT EXISTS idx_comment_author
    ON work_item_comments (author_id);

CREATE TABLE IF NOT EXISTS activity_logs (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    project_id      BIGINT          NOT NULL,
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       BIGINT          NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    actor_emp_id    VARCHAR(50)     NOT NULL,
    description     VARCHAR(500)    NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    timestamp       DATETIME        NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_activity_project
    ON activity_logs (project_id);

CREATE INDEX IF NOT EXISTS idx_activity_entity
    ON activity_logs (entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_activity_timestamp
    ON activity_logs (timestamp);

CREATE TABLE IF NOT EXISTS in_app_notifications (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    recipient_id    BIGINT          NOT NULL,
    title           VARCHAR(150)    NOT NULL,
    message         VARCHAR(1000)   NOT NULL,
    link            VARCHAR(255),
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id) REFERENCES employee (employee_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notification_recipient
    ON in_app_notifications (recipient_id);

CREATE INDEX IF NOT EXISTS idx_notification_read
    ON in_app_notifications (is_read);
