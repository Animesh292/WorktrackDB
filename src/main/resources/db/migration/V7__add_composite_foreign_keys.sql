-- ============================================================
-- V7__add_composite_foreign_keys.sql
-- Priority 1: Enforce complete composite foreign-key constraints
-- at database level for MySQL and H2.
--
-- 1. emp_workspace.emp_id -> employee.emp_id
-- 2. emp_workspace.(dept_id, work_id) -> department.(dept_id, w_id)
-- 3. registration.(department_id, workspace_id) -> department.(dept_id, w_id)
-- 4. project.(dept_id, workspace_id) -> department.(dept_id, w_id)
-- ============================================================

-- 1. emp_workspace -> employee (by emp_id)
ALTER TABLE emp_workspace
    ADD CONSTRAINT fk_emp_workspace_employee
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id)
    ON DELETE CASCADE;

-- 2. emp_workspace -> department (composite: dept_id, work_id)
ALTER TABLE emp_workspace
    ADD CONSTRAINT fk_emp_workspace_department
    FOREIGN KEY (dept_id, work_id) REFERENCES department (dept_id, w_id);

-- 3. registration -> department (composite: department_id, workspace_id)
ALTER TABLE registration
    ADD CONSTRAINT fk_registration_department
    FOREIGN KEY (department_id, workspace_id) REFERENCES department (dept_id, w_id);

-- 4. project -> department (composite: dept_id, workspace_id)
-- Guarantees a project cannot reference a department outside its assigned workspace.
ALTER TABLE project
    ADD CONSTRAINT fk_project_department
    FOREIGN KEY (dept_id, workspace_id) REFERENCES department (dept_id, w_id);
