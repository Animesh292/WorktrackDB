package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="login")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="login_id")
    private Integer loginId;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @OneToOne
    @JoinColumn(name="employee_id", unique = true, nullable = false)
    private Employee employee;

    @Column(name="work_email", unique=true, nullable=false)
    private String workEmail;

    @Column(name="must_change_password", nullable = false)
    private Boolean mustChangePassword;
}
