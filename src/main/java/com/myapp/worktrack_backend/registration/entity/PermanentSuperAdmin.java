package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="permanent_super_admin")
public class PermanentSuperAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @OneToOne
    @JoinColumn(name="employee_id", nullable = false, unique = true)
    private Employee employee;
}
