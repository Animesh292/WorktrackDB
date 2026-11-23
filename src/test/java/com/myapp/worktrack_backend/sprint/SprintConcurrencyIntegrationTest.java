package com.myapp.worktrack_backend.sprint;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.exceptionhandler.ActiveSprintExistsException;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.service.SprintService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:worktrack_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE")
class SprintConcurrencyIntegrationTest {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SprintRepo sprintRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ProjectMemberRepo projectMemberRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private WorkspaceRepo workspaceRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @MockitoBean
    private AuthenticatedEmployeeDetails authEmpDetails;

    private Project testProject;
    private Sprint sprint1;
    private Sprint sprint2;
    private Employee owner;

    @BeforeEach
    void setUp() {
        Workspace ws = new Workspace();
        ws.setWorkspaceName("Concurrency WS");
        ws.setStatus("Active");
        ws = workspaceRepo.save(ws);

        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(ws.getWId());
        dw.setDeptId("CC01");

        Department dept = new Department();
        dept.setDepartmentwkspc(dw);
        dept.setWorkspace(ws);
        dept.setDeptName("Concurrency Dept");
        dept.setStatus("Active");
        dept = departmentRepo.save(dept);

        owner = new Employee();
        owner.setEmpId("EM_CC001");
        owner.setRole("Super-Admin");
        owner.setEmployeeStatus("Active");
        owner.setRegId(999901);
        owner = employeeRepo.save(owner);

        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(owner);

        testProject = new Project();
        testProject.setProjectKey("CONC");
        testProject.setName("Concurrency Project");
        testProject.setWorkspace(ws);
        testProject.setDeptId("CC01");
        testProject.setCreatedByEmpId(owner.getEmpId());
        testProject.setStatus("ACTIVE");
        testProject = projectRepo.save(testProject);

        ProjectMember member = new ProjectMember();
        member.setProject(testProject);
        member.setEmployee(owner);
        member.setRole(ProjectMemberRole.OWNER);
        member.setStatus("ACTIVE");
        member.setAddedByEmpId(owner.getEmpId());
        projectMemberRepo.save(member);

        sprint1 = new Sprint();
        sprint1.setProject(testProject);
        sprint1.setName("Sprint Alpha");
        sprint1.setState(SprintState.PLANNED);
        sprint1.setStartDate(LocalDate.now());
        sprint1.setEndDate(LocalDate.now().plusDays(14));
        sprint1.setCreatedByEmpId(owner.getEmpId());
        sprint1 = sprintRepo.save(sprint1);

        sprint2 = new Sprint();
        sprint2.setProject(testProject);
        sprint2.setName("Sprint Beta");
        sprint2.setState(SprintState.PLANNED);
        sprint2.setStartDate(LocalDate.now().plusDays(15));
        sprint2.setEndDate(LocalDate.now().plusDays(29));
        sprint2.setCreatedByEmpId(owner.getEmpId());
        sprint2 = sprintRepo.save(sprint2);
    }

    @AfterEach
    void tearDown() {
        sprintRepo.deleteAll();
        projectMemberRepo.deleteAll();
        projectRepo.deleteAll();
    }

    @Test
    void testConcurrentSprintStart_enforcesSingleActiveSprintInvariant() throws Exception {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        Long projectId = testProject.getProjectId();
        Long s1Id = sprint1.getId();
        Long s2Id = sprint2.getId();

        Future<?> f1 = executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(owner);
                sprintService.startSprint(projectId, s1Id);
                successCount.incrementAndGet();
            } catch (ActiveSprintExistsException e) {
                rejectedCount.incrementAndGet();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("active sprint")) {
                    rejectedCount.incrementAndGet();
                } else if (e.getCause() instanceof ActiveSprintExistsException) {
                    rejectedCount.incrementAndGet();
                }
            }
        });

        Future<?> f2 = executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(owner);
                sprintService.startSprint(projectId, s2Id);
                successCount.incrementAndGet();
            } catch (ActiveSprintExistsException e) {
                rejectedCount.incrementAndGet();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("active sprint")) {
                    rejectedCount.incrementAndGet();
                } else if (e.getCause() instanceof ActiveSprintExistsException) {
                    rejectedCount.incrementAndGet();
                }
            }
        });

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Release both threads at the exact same instant

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly ONE sprint must transition to ACTIVE
        assertEquals(1, successCount.get(), "Exactly one concurrent startSprint must succeed");
        assertEquals(1, rejectedCount.get(), "The other concurrent startSprint must be rejected");

        // Verify database state: exactly ONE active sprint in project
        long activeSprintsInDb = sprintRepo.countByProjectProjectIdAndState(projectId, SprintState.ACTIVE);
        assertEquals(1L, activeSprintsInDb, "Database must contain exactly 1 ACTIVE sprint for the project");
    }
}
