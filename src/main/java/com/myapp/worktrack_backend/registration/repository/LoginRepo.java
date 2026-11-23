package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepo extends JpaRepository<Login, Integer> {
    Optional<Login> findByWorkEmail(String workEmail);
    Optional<Login> findByEmployeeEmployeeId(Long employeeId);
}
