-- ============================================================
-- V1__initial_schema.sql
-- WorkTrack baseline schema — matches JPA entity mappings
-- Applied as Flyway baseline (baseline-on-migrate=true for
-- existing databases; runs in full on new installs).
-- ============================================================

-- ── workspace ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS workspace (
    w_id               INT          NOT NULL AUTO_INCREMENT,
    workspace_name     VARCHAR(255),
    city               VARCHAR(255),
    state              VARCHAR(255),
    country            VARCHAR(255),
    address            VARCHAR(255),
    headquarters_name  VARCHAR(255),
    status             VARCHAR(50),
    PRIMARY KEY (w_id)
);

-- ── employee ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employee (
    employee_id      BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id           VARCHAR(50)   UNIQUE,
    reg_id           INT           NOT NULL UNIQUE,
    role             VARCHAR(50)   NOT NULL,
    employee_status  VARCHAR(50)   NOT NULL,
    PRIMARY KEY (employee_id)
);

-- ── department ───────────────────────────────────────────────
-- Composite PK: (dept_id, w_id)
CREATE TABLE IF NOT EXISTS department (
    dept_id               VARCHAR(5)      NOT NULL,
    w_id                  INT             NOT NULL,
    dept_name             VARCHAR(255),
    dept_head_employee_id BIGINT,
    capacity              INT,
    budget                DECIMAL(12, 2),
    status                VARCHAR(50),
    PRIMARY KEY (dept_id, w_id),
    CONSTRAINT fk_dept_workspace
        FOREIGN KEY (w_id) REFERENCES workspace (w_id),
    CONSTRAINT fk_dept_head
        FOREIGN KEY (dept_head_employee_id) REFERENCES employee (employee_id)
);

-- ── registration ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS registration (
    reg_id              INT          NOT NULL AUTO_INCREMENT,
    first_name          VARCHAR(255),
    last_name           VARCHAR(255),
    work_email          VARCHAR(255),
    sex                 CHAR(1),
    phn_num             VARCHAR(50),
    workspace_id        INT          NOT NULL,
    department_id       VARCHAR(5)   NOT NULL,
    experience          FLOAT,
    birthdate           DATE,
    role                VARCHAR(50),
    application_status  VARCHAR(50),
    registration_uuid   VARCHAR(255),
    PRIMARY KEY (reg_id)
);

-- ── emp_workspace ─────────────────────────────────────────────
-- Composite PK: (emp_id, work_id)
-- emp_id references employee.emp_id (string business ID, not numeric PK)
CREATE TABLE IF NOT EXISTS emp_workspace (
    emp_id   VARCHAR(50) NOT NULL,
    work_id  INT         NOT NULL,
    dept_id  VARCHAR(5),
    PRIMARY KEY (emp_id, work_id)
);

-- ── login ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS login (
    login_id            INT          NOT NULL AUTO_INCREMENT,
    password_hash       VARCHAR(255) NOT NULL,
    employee_id         BIGINT       NOT NULL UNIQUE,
    work_email          VARCHAR(255) NOT NULL UNIQUE,
    must_change_password BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (login_id),
    CONSTRAINT fk_login_employee
        FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
);

-- ── permanent_super_admin ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS permanent_super_admin (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL UNIQUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_psa_employee
        FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
);

-- ── Indexes for common query patterns ────────────────────────
CREATE INDEX IF NOT EXISTS idx_registration_uuid
    ON registration (registration_uuid);

CREATE INDEX IF NOT EXISTS idx_registration_email
    ON registration (work_email);

CREATE INDEX IF NOT EXISTS idx_emp_workspace_work_id
    ON emp_workspace (work_id);

CREATE INDEX IF NOT EXISTS idx_emp_workspace_dept
    ON emp_workspace (dept_id, work_id);
