package com.myapp.worktrack_backend.security;

import com.myapp.worktrack_backend.registration.auth.JWTAuthFilter;
import com.myapp.worktrack_backend.registration.auth.JWTService;
import com.myapp.worktrack_backend.registration.controller.OpenApiController;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Login;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.LoginRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityIntegrationTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private EmployeeRepo empRepo;

    @Mock
    private LoginRepo loginRepo;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthFilter jwtAuthFilter;

    private Employee activeEmployee;
    private Employee inactiveEmployee;
    private Login normalLogin;
    private Login passwordChangeRequiredLogin;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        activeEmployee = new Employee();
        activeEmployee.setEmployeeId(101L);
        activeEmployee.setEmpId("EM00101");
        activeEmployee.setRole("Staff");
        activeEmployee.setEmployeeStatus("Active");

        inactiveEmployee = new Employee();
        inactiveEmployee.setEmployeeId(102L);
        inactiveEmployee.setEmpId("EM00102");
        inactiveEmployee.setRole("Staff");
        inactiveEmployee.setEmployeeStatus("Suspended");

        normalLogin = new Login();
        normalLogin.setEmployee(activeEmployee);
        normalLogin.setMustChangePassword(false);

        passwordChangeRequiredLogin = new Login();
        passwordChangeRequiredLogin.setEmployee(activeEmployee);
        passwordChangeRequiredLogin.setMustChangePassword(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_validToken_activeEmployee_authenticatesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        request.setServletPath("/projects");

        when(jwtService.validateAndExtract("valid.jwt.token")).thenReturn("101");
        when(empRepo.findById(101L)).thenReturn(Optional.of(activeEmployee));
        when(loginRepo.findByEmployeeEmployeeId(101L)).thenReturn(Optional.of(normalLogin));

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("101", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test
    void doFilter_noAuthHeader_passesThroughWithoutAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api-docs");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_optionsPreflight_bypassesFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("OPTIONS");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_inactiveEmployee_rejectsWith401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer inactive.jwt.token");
        request.setServletPath("/projects");

        when(jwtService.validateAndExtract("inactive.jwt.token")).thenReturn("102");
        when(empRepo.findById(102L)).thenReturn(Optional.of(inactiveEmployee));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_mustChangePassword_blocksNormalEndpointWith403Forbidden() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer change.password.token");
        request.setServletPath("/projects");

        when(jwtService.validateAndExtract("change.password.token")).thenReturn("101");
        when(empRepo.findById(101L)).thenReturn(Optional.of(activeEmployee));
        when(loginRepo.findByEmployeeEmployeeId(101L)).thenReturn(Optional.of(passwordChangeRequiredLogin));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_mustChangePassword_allowsPasswordChangeEndpoint() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer change.password.token");
        request.setServletPath("/login/change-password");

        when(jwtService.validateAndExtract("change.password.token")).thenReturn("101");
        when(empRepo.findById(101L)).thenReturn(Optional.of(activeEmployee));

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_anonymousPasswordChange_leavesUnauthenticatedForSpringSecurity401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/login/change-password");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void openApiController_servesApiDocsAndSwaggerUi() throws IOException {
        OpenApiController controller = new OpenApiController();

        ResponseEntity<String> apiDocsResponse = controller.getOpenApiSpecification();
        assertEquals(HttpStatus.OK, apiDocsResponse.getStatusCode());
        assertNotNull(apiDocsResponse.getBody());
        assertTrue(apiDocsResponse.getBody().contains("WorkTrack REST API"));

        ResponseEntity<String> swaggerUiResponse = controller.getSwaggerUi();
        assertEquals(HttpStatus.OK, swaggerUiResponse.getStatusCode());
        assertNotNull(swaggerUiResponse.getBody());
        assertTrue(swaggerUiResponse.getBody().contains("swagger-ui"));
    }
}
