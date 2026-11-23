package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name="department")
public class Department implements Serializable {
    @EmbeddedId
    private DepartmentWorkspace departmentwkspc;

    @MapsId("wId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="w_id", nullable=false)
    private Workspace workspace;


    @Column(name="dept_name")
    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dept_head_employee_id", referencedColumnName = "employee_id")
    private Employee departmentHead;

    @Column(name="capacity")
    private Integer capacity;

    @Column(name="budget", precision=12, scale=2)
    private BigDecimal budget;

    @Column(name="status")
    private String status;


}
