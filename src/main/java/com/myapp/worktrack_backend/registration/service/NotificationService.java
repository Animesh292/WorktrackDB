package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.auth.AuthenticatedEmployeeDetails;
import com.myapp.worktrack_backend.registration.dto.InAppNotificationResponseDTO;
import com.myapp.worktrack_backend.registration.entity.Employee;
import com.myapp.worktrack_backend.registration.entity.InAppNotification;
import com.myapp.worktrack_backend.registration.event.ProjectActivityEvent;
import com.myapp.worktrack_backend.registration.repository.EmployeeRepo;
import com.myapp.worktrack_backend.registration.repository.InAppNotificationRepo;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final InAppNotificationRepo notificationRepo;
    private final EmployeeRepo employeeRepo;
    private final AuthenticatedEmployeeDetails authEmpDetails;

    public NotificationService(InAppNotificationRepo notificationRepo,
                               EmployeeRepo employeeRepo,
                               AuthenticatedEmployeeDetails authEmpDetails) {
        this.notificationRepo = notificationRepo;
        this.employeeRepo = employeeRepo;
        this.authEmpDetails = authEmpDetails;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(ProjectActivityEvent event) {
        if (event == null || event.recipientEmployeeId() == null) {
            return;
        }

        employeeRepo.findById(event.recipientEmployeeId()).ifPresent(recipient -> {
            InAppNotification n = new InAppNotification();
            n.setRecipient(recipient);
            n.setTitle(event.notificationTitle() != null ? event.notificationTitle() : "WorkTrack Alert");
            n.setMessage(event.notificationMessage() != null ? event.notificationMessage() : event.description());
            n.setLink(event.notificationLink());
            n.setRead(false);
            notificationRepo.save(n);
        });
    }

    @Transactional(readOnly = true)
    public Page<InAppNotificationResponseDTO> getMyNotifications(boolean unreadOnly, Pageable pageable) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        if (unreadOnly) {
            return notificationRepo.findByRecipientEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(
                    actor.getEmployeeId(), pageable).map(InAppNotificationResponseDTO::fromEntity);
        }
        return notificationRepo.findByRecipientEmployeeIdOrderByCreatedAtDesc(
                actor.getEmployeeId(), pageable).map(InAppNotificationResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        return notificationRepo.countByRecipientEmployeeIdAndIsReadFalse(actor.getEmployeeId());
    }

    @Transactional
    public InAppNotificationResponseDTO markAsRead(Long notificationId) {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        InAppNotification n = notificationRepo.findByIdAndRecipientEmployeeId(notificationId, actor.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        n.setRead(true);
        return InAppNotificationResponseDTO.fromEntity(notificationRepo.save(n));
    }

    @Transactional
    public void markAllAsRead() {
        Employee actor = authEmpDetails.getAuthEmployeeDetails();
        Page<InAppNotification> unreadPage = notificationRepo.findByRecipientEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(
                actor.getEmployeeId(), Pageable.unpaged());
        List<InAppNotification> unread = unreadPage.getContent();
        unread.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(unread);
    }
}
