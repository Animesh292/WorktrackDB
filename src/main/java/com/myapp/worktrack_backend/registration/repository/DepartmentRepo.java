package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.DepartmentWorkspace;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface DepartmentRepo extends JpaRepository<Department, DepartmentWorkspace> {
    public List<Department> findByWorkspaceAndStatus(Workspace workspace, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Department d where d.departmentwkspc = :id")
    Optional<Department> findByIdForUpdate(@Param("id") DepartmentWorkspace id);
}
