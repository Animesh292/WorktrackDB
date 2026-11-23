package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmpId(String empId);

    @Query("SELECT e.employeeStatus as status, COUNT(e) as count FROM Employee e GROUP BY e.employeeStatus")
    List<com.myapp.worktrack_backend.registration.repository.projection.StatusCountProjection> countAllGroupedByEmployeeStatus();
}
