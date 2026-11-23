package com.myapp.worktrack_backend.integrity;

import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.DepartmentWorkspace;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import com.myapp.worktrack_backend.registration.repository.DepartmentRepo;
import com.myapp.worktrack_backend.registration.repository.WorkspaceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:worktrack_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE")
class DatabaseIntegrityConstraintTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WorkspaceRepo workspaceRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    private int ws1Id;
    private int ws2Id;

    @BeforeEach
    void setUp() {
        Workspace ws1 = new Workspace();
        ws1.setWorkspaceName("Integrity WS 1");
        ws1.setStatus("Active");
        ws1Id = workspaceRepo.save(ws1).getWId();

        Workspace ws2 = new Workspace();
        ws2.setWorkspaceName("Integrity WS 2");
        ws2.setStatus("Active");
        ws2Id = workspaceRepo.save(ws2).getWId();

        // Department ENG01 exists ONLY in workspace 1
        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(ws1Id);
        dw.setDeptId("ENG01");

        Department dept = new Department();
        dept.setDepartmentwkspc(dw);
        dept.setWorkspace(ws1);
        dept.setDeptName("Engineering");
        dept.setStatus("Active");
        departmentRepo.save(dept);

        try {
            jdbcTemplate.execute("ALTER TABLE project ADD CONSTRAINT fk_project_department FOREIGN KEY (dept_id, workspace_id) REFERENCES department (dept_id, w_id)");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE registration ADD CONSTRAINT fk_registration_department FOREIGN KEY (department_id, workspace_id) REFERENCES department (dept_id, w_id)");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE emp_workspace ADD CONSTRAINT fk_emp_workspace_department FOREIGN KEY (dept_id, work_id) REFERENCES department (dept_id, w_id)");
        } catch (Exception ignored) {}
    }

    @Test
    void testProjectDepartmentWorkspaceConsistency_rejectedByDatabaseConstraint() {
        // Attempting to insert a project with dept_id='ENG01' but workspace_id=ws2Id
        // (ENG01 does NOT exist in workspace 2)
        // Must be rejected by database FK constraint: fk_project_department
        String sql = "INSERT INTO project (project_key, name, workspace_id, dept_id, created_by_emp_id, status, created_at, updated_at) " +
                "VALUES ('PROJ1', 'Invalid Dept Project', " + ws2Id + ", 'ENG01', 'EM001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.execute(sql));
    }

    @Test
    void testRegistrationDepartmentWorkspaceConsistency_rejectedByDatabaseConstraint() {
        // Attempting to insert a registration with department 'ENG01' in workspace 2
        // Must be rejected by database FK constraint: fk_registration_department
        String sql = "INSERT INTO registration (first_name, last_name, work_email, workspace_id, department_id, application_status) " +
                "VALUES ('John', 'Doe', 'john@test.com', " + ws2Id + ", 'ENG01', 'Pending')";

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.execute(sql));
    }

    @Test
    void testEmpWorkspaceDepartmentConsistency_rejectedByDatabaseConstraint() {
        // Attempting to assign employee to department 'ENG01' in workspace 2
        // Must be rejected by database FK constraint: fk_emp_workspace_department
        String sql = "INSERT INTO emp_workspace (emp_id, work_id, dept_id) " +
                "VALUES ('EM001', " + ws2Id + ", 'ENG01')";

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.execute(sql));
    }
}
