package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepo extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByProjectIdOrderByTimestampDesc(Long projectId, Pageable pageable);

    Page<ActivityLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId, Pageable pageable);

    Page<ActivityLog> findByProjectIdAndEntityTypeOrderByTimestampDesc(Long projectId, String entityType, Pageable pageable);
}
