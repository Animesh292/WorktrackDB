package com.myapp.worktrack_backend.registration.entity;

import com.myapp.worktrack_backend.registration.auth.AuthorizableResource;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="employee")
public class Employee implements AuthorizableResource {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="employee_id")
    private long employeeId;


    @Column(name="emp_id", unique=true, length=50)
    private String empId;

    @Column(name="reg_id", nullable=false, unique = true)
    private int regId;

    @Column(name="role", nullable=false)
    private String role;

    @Column(name="employee_status", nullable=false)
    private String employeeStatus;
}
