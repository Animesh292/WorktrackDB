package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Sprint;
import com.myapp.worktrack_backend.registration.entity.SprintState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SprintRepo extends JpaRepository<Sprint, Long> {

    Optional<Sprint> findByIdAndProjectProjectId(Long id, Long projectId);

    Optional<Sprint> findByProjectProjectIdAndState(Long projectId, SprintState state);

    boolean existsByProjectProjectIdAndState(Long projectId, SprintState state);

    Page<Sprint> findByProjectProjectId(Long projectId, Pageable pageable);

    Page<Sprint> findByProjectProjectIdAndState(Long projectId, SprintState state, Pageable pageable);

    long countByState(SprintState state);

    long countByProjectProjectIdAndState(Long projectId, SprintState state);

    long countByProjectWorkspaceWIdAndProjectDeptIdAndState(int wId, String deptId, SprintState state);
}
