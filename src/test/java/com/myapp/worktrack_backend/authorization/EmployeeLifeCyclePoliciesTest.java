package com.myapp.worktrack_backend.authorization;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.auth.EmployeeLifecyclePolicies;
import com.myapp.worktrack_backend.registration.auth.RegistrationApprovalContext;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.PermanentSuperAdmin;
import com.myapp.worktrack_backend.registration.entity.Registration;
import com.myapp.worktrack_backend.registration.repository.EmployeeWorkspaceRepo;
import com.myapp.worktrack_backend.registration.repository.PermanentSuperAdminRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeLifeCyclePoliciesTest {

    @Mock
    private EmployeeWorkspaceRepo employeeWorkspaceRepo;

    @Mock
    private PermanentSuperAdminRepo permanentSuperAdminRepo;

    @Mock
    private AuthenticatedEmployeeDetails authenticatedEmployeeDetails;

    @InjectMocks
    private EmployeeLifecyclePolicies employeeLifecyclePolicies;


    @Test
    void adminCanPromoteStaffWithinScope() {
        //1. Arrange
        //create authenticate actor
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setRole("Admin");
        actor.setEmpId("EM00001");
        actor.setEmployeeStatus("Active");

        //create target employee
        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setEmpId("EM00002");
        target.setRole("Staff");
        target.setEmployeeStatus("Active");

        //Mock actor to be authenticated user
        when(authenticatedEmployeeDetails.getAuthEmployeeDetails())
                .thenReturn(actor);

        //Mock target and actor in same workspace scope
        when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId()))
                .thenReturn(1L);

        //Act: since scoped and authenticated actor action permitted
        assertDoesNotThrow(() -> employeeLifecyclePolicies.changeRole(target, "Admin"));
    }

    @Test
    void cannotPromoteStaffOutsideScope() {
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM00001");
        actor.setRole("Admin");
        actor.setEmployeeStatus("Active");

        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setEmployeeStatus("Active");
        target.setEmpId("EM00002");
        target.setRole("Staff");

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);

        when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId())).thenReturn(0L); //outside scope

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.changeRole(target, "Admin"));
    }

    @Test
    void adminCannotChangeAnotherAdminRole() {
        Employee actor = new Employee();
        actor.setEmployeeId(8L);
        actor.setEmpId("EM00001");
        actor.setEmployeeStatus("Active");
        actor.setRole("Admin");


        Employee target = new Employee();
        target.setEmployeeId(6L);
        target.setEmpId("EM00002");
        target.setEmployeeStatus("Active");
        target.setRole("Staff");

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);

 //      when(permanentSuperAdminRepo.existsByEmployeeEmployeeId(actor.getEmployeeId())).thenReturn(false);

       when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId())).thenReturn(0L);

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.changeRole(target, "Admin"));
    }

    @Test
    void terminateEmployee() {
        Employee actor = new Employee();
        actor.setEmployeeId(13L);
        actor.setEmpId("EM00002");
        actor.setEmployeeStatus("Active");
        actor.setRole("Super-Admin");

        Employee target = new Employee();
        target.setEmployeeId(11L);
        target.setEmpId("EM00003");
        target.setEmployeeStatus("Active");
        target.setRole("Super-Admin");

       when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);
      //  when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId())).thenReturn(true);
        when(permanentSuperAdminRepo.existsByEmployeeEmployeeId(actor.getEmployeeId())).thenReturn(true);
      assertDoesNotThrow(() -> employeeLifecyclePolicies.terminateEmployee(target));
      //  assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.terminateEmployee(target));
        verify(permanentSuperAdminRepo).existsByEmployeeEmployeeId(actor.getEmployeeId()); //this will give the policy consulted th permanent super-admin repo for access
    }

    @Test
    void testActivateEmployeeWithStates() {
        //only a suspended can be activated
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM00001");
        actor.setRole("Super-Admin");
        actor.setEmployeeStatus("Active");

        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setRole("Admin");
        target.setEmployeeStatus("Suspended");
        target.setEmpId("EM00004");

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);

        assertDoesNotThrow(() -> employeeLifecyclePolicies.activateEmployee(target));
    }
    @Test
    void testActivateEmployeeInvalidStates() {
        //only a suspended can be activated


        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setRole("Admin");
        target.setEmployeeStatus("Active");
        target.setEmpId("EM00004");

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.activateEmployee(target));
    }

    @Test
    void testActivateEmployeeInvalidStates_01() {
        //only a suspended can be activated


        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setRole("Admin");
        target.setEmployeeStatus("Terminated");
        target.setEmpId("EM00004");

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.activateEmployee(target));
    }

    @Test
    void testSuspendEmployeeValidStates() {
        Employee target = new Employee();
        target.setEmpId("EM0001");
        target.setEmployeeId(3L);
        target.setEmployeeStatus("Suspended");
        target.setRole("Admin");

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.suspendEmployee(target));
    }

    @Test
    void testSuspendEmployeeValidState_Terminated() {
        Employee target = new Employee();
        target.setEmpId("EM00002");
        target.setEmployeeId(2L);
        target.setEmployeeStatus("Terminated");
        target.setRole("Admin");

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.suspendEmployee(target));
    }

    @Test
    void testSuspendEmployeeAuthorization() {
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM0001");
        actor.setEmployeeStatus("Active");
        actor.setRole("Admin");

        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setEmpId("EM0002");
        target.setEmployeeStatus("Active");
        target.setRole("Staff");

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId())).thenReturn(1L);

        assertDoesNotThrow(() -> employeeLifecyclePolicies.suspendEmployee(target));
    }

    @Test
    void testSuspendEmployeeAuthorizationOutsideScope() {
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM0001");
        actor.setEmployeeStatus("Active");
        actor.setRole("Admin");

        Employee target = new Employee();
        target.setEmployeeId(2L);
        target.setEmpId("EM0002");
        target.setEmployeeStatus("Active");
        target.setRole("Staff");

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(employeeWorkspaceRepo.isScopeValid(target.getEmpId(), actor.getEmpId())).thenReturn(0L);

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.suspendEmployee(target));
    }

    @Test //test for role authorization check
    void testApproveRegistrationWithStaffActor() {
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmpId("EM00003");
        actor.setRole("Staff");
        actor.setEmployeeStatus("Active");

        Registration applicant = new Registration();
        applicant.setApplicationStatus("Pending");
        applicant.setRole("Admin");
        applicant.setWorkEmail("xylo@example.com");
        applicant.setBirthdate(LocalDate.parse("2001-12-11"));

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.approveRegistration(applicant, new RegistrationApprovalContext("Admin", "D1", 1)));
    }

    @Test
    void testRejectRegistrationStateValidation() {
        Employee actor = new Employee();
        actor.setEmployeeId(22L);
        actor.setEmpId("EM0005");
        actor.setRole("Admin");
        actor.setEmployeeStatus("Active");

        Registration applicant = new Registration();
        applicant.setApplicationStatus("Approved");
        applicant.setRole("Staff");
        applicant.setWorkEmail("xylo@example.com");
        applicant.setBirthdate(LocalDate.parse("2021-12-11"));

        assertThrows(AccessDeniedException.class, () -> employeeLifecyclePolicies.rejectRegistration(applicant, null));
    }

    @Test
    void testApproveRegistrationWithAuthorization() {
        Employee actor = new Employee();
        actor.setEmployeeId(1L);
        actor.setEmployeeStatus("Active");
        actor.setRole("Admin");
        actor.setEmpId("EM00003");

        Registration applicant = new Registration();
        applicant.setRole("Staff");
        applicant.setApplicationStatus("Pending");
        applicant.setWorkEmail("xylo@example.com");
        applicant.setBirthdate(LocalDate.parse("2019-11-12"));

        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);
        when(employeeWorkspaceRepo.isAssignedToWorkspaceAndDepartment(actor.getEmpId(), 1, "D1")).thenReturn(1L);
        assertDoesNotThrow(() -> employeeLifecyclePolicies.approveRegistration(applicant, new RegistrationApprovalContext("Staff", "D1", 1)));
    }

    @Test //actor is authorized and performs rejection on the application
    void testRegistrationRejectionAuthorization() {
        Employee actor = new Employee();
        actor.setEmpId("EM00001");
        actor.setRole("Admin");
        actor.setEmployeeId(1L);
        actor.setEmployeeStatus("Active");

        Registration applicant = new Registration();
        applicant.setApplicationStatus("Pending");
        applicant.setRole("Staff");
        applicant.setWorkEmail("xylo@example.com");
        applicant.setWorkspaceId(1);

        //mock the dependancies
        when(authenticatedEmployeeDetails.getAuthEmployeeDetails()).thenReturn(actor);
        //assert the action with normal result
        when(employeeWorkspaceRepo.isAssignedToWorkspace(actor.getEmpId(), 1)).thenReturn(1L);
        assertDoesNotThrow(() -> employeeLifecyclePolicies.rejectRegistration(applicant, null));
    }
}
