package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InAppNotificationRepo extends JpaRepository<InAppNotification, Long> {

    Page<InAppNotification> findByRecipientEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    Page<InAppNotification> findByRecipientEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    long countByRecipientEmployeeIdAndIsReadFalse(Long employeeId);

    Optional<InAppNotification> findByIdAndRecipientEmployeeId(Long id, Long employeeId);
}
