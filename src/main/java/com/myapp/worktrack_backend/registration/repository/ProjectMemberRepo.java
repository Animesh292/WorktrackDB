package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {

    Page<ProjectMember> findByProjectProjectIdAndStatus(Long projectId, String status, Pageable pageable);

    Optional<ProjectMember> findByProjectProjectIdAndEmployeeEmployeeId(Long projectId, Long employeeId);

    Optional<ProjectMember> findByProjectProjectIdAndEmployeeEmployeeIdAndStatus(Long projectId, Long employeeId, String status);

    boolean existsByProjectProjectIdAndEmployeeEmployeeIdAndStatus(Long projectId, Long employeeId, String status);

    Optional<ProjectMember> findByIdAndProjectProjectId(Long id, Long projectId);

    Page<ProjectMember> findByEmployeeEmployeeIdAndStatus(Long employeeId, String status, Pageable pageable);

    List<ProjectMember> findByEmployeeEmployeeIdAndStatus(Long employeeId, String status);

    long countByEmployeeEmployeeIdAndStatus(Long employeeId, String status);
}
