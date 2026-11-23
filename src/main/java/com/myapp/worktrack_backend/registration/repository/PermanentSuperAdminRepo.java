package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.PermanentSuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermanentSuperAdminRepo extends JpaRepository<PermanentSuperAdmin, Long> {
    boolean existsByEmployeeEmployeeId(Long employeeId);
}
