package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface RegistrationRepo extends JpaRepository<Registration, Integer> {
    boolean existsByWorkEmail(String workEmail);
    Optional<Registration> findByRegistrationUUID(String regUUID);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Registration r where r.registrationUUID = :registrationUUID")
    Optional<Registration> findByRegistrationUUIDForUpdate(@Param("registrationUUID") String registrationUUID);
//    public Boolean existsByDepartment(String department);
//    public Boolean existsByWorkspace(String workspace);
}
