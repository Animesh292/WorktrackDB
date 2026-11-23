package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.WorkItemComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkItemCommentRepo extends JpaRepository<WorkItemComment, Long> {

    Page<WorkItemComment> findByWorkItemIdAndIsDeletedFalseOrderByCreatedAtAsc(Long workItemId, Pageable pageable);

    Optional<WorkItemComment> findByIdAndWorkItemId(Long id, Long workItemId);
}
