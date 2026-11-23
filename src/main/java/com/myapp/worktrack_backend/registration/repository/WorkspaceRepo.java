package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepo extends JpaRepository<Workspace, Integer> {
    public List<Workspace> findByStatusIgnoreCase(String status);
}
