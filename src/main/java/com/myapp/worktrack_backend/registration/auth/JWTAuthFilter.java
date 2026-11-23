package com.myapp.worktrack_backend.registration.auth;

import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Login;
import com.myapp.worktrack_backend.registration.exceptionhandler.EmployeeNotFoundException;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.LoginRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final EmployeeRepo empRepo;
    private final LoginRepo loginRepo;

    public JWTAuthFilter(JWTService jwtService, EmployeeRepo empRepo, LoginRepo loginRepo) {
        this.jwtService=jwtService;this.empRepo=empRepo;this.loginRepo=loginRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) { //preflight HTTP requests bypassing security checks and goes to rest of filter chains.
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if(header !=null && header.startsWith("Bearer ")) {
            String token = header.substring(7); //extract the token

            try {
                String employeeId = jwtService.validateAndExtract(token);
                Employee employee = empRepo.findById(Long.parseLong(employeeId)).orElseThrow(() -> new EmployeeNotFoundException("Employee not found!"));
                if (!"Active".equals(employee.getEmployeeStatus())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token belongs to an inactive account");
                    return;
                }
                boolean passwordChangeRequest = "/login/change-password".equals(request.getServletPath());
                if (!passwordChangeRequest && loginRepo.findByEmployeeEmployeeId(employee.getEmployeeId())
                        .map(Login::getMustChangePassword).orElse(false)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Password change required");
                    return;
                }

                String role = employee.getRole();
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
                var auth = new UsernamePasswordAuthenticationToken(employeeId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (Exception exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}
