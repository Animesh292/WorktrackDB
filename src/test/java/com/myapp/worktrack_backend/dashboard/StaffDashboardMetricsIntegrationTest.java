package com.myapp.worktrack_backend.dashboard;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.StaffDashboardDTO;
import com.myapp.worktrack_backend.registration.entity.*;
import com.myapp.worktrack_backend.registration.repository.*;
import com.myapp.worktrack_backend.registration.service.DashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:worktrack_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE")
class StaffDashboardMetricsIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private WorkspaceRepo workspaceRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ProjectMemberRepo projectMemberRepo;

    @Autowired
    private SprintRepo sprintRepo;

    @Autowired
    private WorkItemRepo workItemRepo;

    @MockitoBean
    private AuthenticatedEmployeeDetails authEmpDetails;

    private Employee staff;
    private Project myProject;
    private Project otherProject;

    @BeforeEach
    void setUp() {
        Workspace ws = new Workspace();
        ws.setWorkspaceName("Metrics WS");
        ws.setStatus("Active");
        ws = workspaceRepo.save(ws);

        DepartmentWorkspace dw = new DepartmentWorkspace();
        dw.setWId(ws.getWId());
        dw.setDeptId("MET01");

        Department dept = new Department();
        dept.setDepartmentwkspc(dw);
        dept.setWorkspace(ws);
        dept.setDeptName("Metrics Dept");
        dept.setStatus("Active");
        dept = departmentRepo.save(dept);

        staff = new Employee();
        staff.setEmpId("EM_STAFF_METRICS");
        staff.setRole("Staff");
        staff.setEmployeeStatus("Active");
        staff.setRegId(998801);
        staff = employeeRepo.save(staff);

        when(authEmpDetails.getAuthEmployeeDetails()).thenReturn(staff);

        // Project 1: Staff is an active member
        myProject = new Project();
        myProject.setProjectKey("MYPRJ");
        myProject.setName("My Project");
        myProject.setWorkspace(ws);
        myProject.setDeptId("MET01");
        myProject.setCreatedByEmpId(staff.getEmpId());
        myProject.setStatus("ACTIVE");
        myProject = projectRepo.save(myProject);

        ProjectMember member = new ProjectMember();
        member.setProject(myProject);
        member.setEmployee(staff);
        member.setRole(ProjectMemberRole.CONTRIBUTOR);
        member.setStatus("ACTIVE");
        member.setAddedByEmpId(staff.getEmpId());
        projectMemberRepo.save(member);

        // Project 2: Staff is NOT a member
        otherProject = new Project();
        otherProject.setProjectKey("OTHR");
        otherProject.setName("Other Project");
        otherProject.setWorkspace(ws);
        otherProject.setDeptId("MET01");
        otherProject.setCreatedByEmpId("ANOTHER_EMP");
        otherProject.setStatus("ACTIVE");
        otherProject = projectRepo.save(otherProject);

        // Active sprint on myProject
        Sprint activeSprint = new Sprint();
        activeSprint.setProject(myProject);
        activeSprint.setName("Active Sprint");
        activeSprint.setState(SprintState.ACTIVE);
        activeSprint.setStartDate(LocalDate.now());
        activeSprint.setEndDate(LocalDate.now().plusDays(14));
        activeSprint.setCreatedByEmpId(staff.getEmpId());
        activeSprint = sprintRepo.save(activeSprint);

        // Planned sprint on myProject
        Sprint plannedSprint = new Sprint();
        plannedSprint.setProject(myProject);
        plannedSprint.setName("Planned Sprint");
        plannedSprint.setState(SprintState.PLANNED);
        plannedSprint.setStartDate(LocalDate.now().plusDays(15));
        plannedSprint.setEndDate(LocalDate.now().plusDays(29));
        plannedSprint.setCreatedByEmpId(staff.getEmpId());
        plannedSprint = sprintRepo.save(plannedSprint);

        // Active sprint on otherProject
        Sprint otherActiveSprint = new Sprint();
        otherActiveSprint.setProject(otherProject);
        otherActiveSprint.setName("Other Active Sprint");
        otherActiveSprint.setState(SprintState.ACTIVE);
        otherActiveSprint.setStartDate(LocalDate.now());
        otherActiveSprint.setEndDate(LocalDate.now().plusDays(14));
        otherActiveSprint.setCreatedByEmpId("ANOTHER_EMP");
        otherActiveSprint = sprintRepo.save(otherActiveSprint);

        // Work Item 1: Assigned to staff in ACTIVE sprint of myProject -> MUST BE COUNTED (+1)
        WorkItem item1 = new WorkItem();
        item1.setProject(myProject);
        item1.setItemKey("MYPRJ-1");
        item1.setItemNumber(1);
        item1.setTitle("Task in active sprint");
        item1.setType(WorkItemType.WORK_ITEM);
        item1.setStatus(WorkItemStatus.IN_PROGRESS);
        item1.setPriority(WorkItemPriority.HIGH);
        item1.setAssignee(staff);
        item1.setReporterEmpId(staff.getEmpId());
        item1.setSprintId(activeSprint.getId());
        workItemRepo.save(item1);

        // Work Item 2: Assigned to staff in ACTIVE sprint of myProject -> MUST BE COUNTED (+1)
        WorkItem item2 = new WorkItem();
        item2.setProject(myProject);
        item2.setItemKey("MYPRJ-2");
        item2.setItemNumber(2);
        item2.setTitle("Second task in active sprint");
        item2.setType(WorkItemType.USER_OUTCOME);
        item2.setStatus(WorkItemStatus.READY);
        item2.setPriority(WorkItemPriority.MEDIUM);
        item2.setAssignee(staff);
        item2.setReporterEmpId(staff.getEmpId());
        item2.setSprintId(activeSprint.getId());
        workItemRepo.save(item2);

        // Work Item 3: Assigned to staff in PLANNED sprint of myProject -> MUST NOT BE COUNTED (0)
        WorkItem item3 = new WorkItem();
        item3.setProject(myProject);
        item3.setItemKey("MYPRJ-3");
        item3.setItemNumber(3);
        item3.setTitle("Task in planned sprint");
        item3.setType(WorkItemType.WORK_ITEM);
        item3.setStatus(WorkItemStatus.BACKLOG);
        item3.setPriority(WorkItemPriority.LOW);
        item3.setAssignee(staff);
        item3.setReporterEmpId(staff.getEmpId());
        item3.setSprintId(plannedSprint.getId());
        workItemRepo.save(item3);

        // Work Item 4: Assigned to staff with NO sprint (Backlog) -> MUST NOT BE COUNTED (0)
        WorkItem item4 = new WorkItem();
        item4.setProject(myProject);
        item4.setItemKey("MYPRJ-4");
        item4.setItemNumber(4);
        item4.setTitle("Backlog task");
        item4.setType(WorkItemType.WORK_ITEM);
        item4.setStatus(WorkItemStatus.BACKLOG);
        item4.setPriority(WorkItemPriority.MEDIUM);
        item4.setAssignee(staff);
        item4.setReporterEmpId(staff.getEmpId());
        item4.setSprintId(null);
        workItemRepo.save(item4);

        // Work Item 5: Assigned to staff in ACTIVE sprint of OTHER project (not member) -> MUST NOT BE COUNTED (0)
        WorkItem item5 = new WorkItem();
        item5.setProject(otherProject);
        item5.setItemKey("OTHR-1");
        item5.setItemNumber(1);
        item5.setTitle("Task in non-member project");
        item5.setType(WorkItemType.WORK_ITEM);
        item5.setStatus(WorkItemStatus.IN_PROGRESS);
        item5.setPriority(WorkItemPriority.HIGH);
        item5.setAssignee(staff);
        item5.setReporterEmpId("ANOTHER_EMP");
        item5.setSprintId(otherActiveSprint.getId());
        workItemRepo.save(item5);
    }

    @AfterEach
    void tearDown() {
        workItemRepo.deleteAll();
        sprintRepo.deleteAll();
        projectMemberRepo.deleteAll();
        projectRepo.deleteAll();
    }

    @Test
    void testStaffDashboard_activeSprintMetric_reflectsActualDatabaseScope() {
        StaffDashboardDTO dashboard = dashboardService.getStaffDashboard();

        // Exactly 2 items (item1 and item2) belong to active sprint on a member project
        assertEquals(2L, dashboard.myActiveSprintItemsCount(),
                "Active sprint workload must count only items in actual ACTIVE sprints for member projects");
    }
}
