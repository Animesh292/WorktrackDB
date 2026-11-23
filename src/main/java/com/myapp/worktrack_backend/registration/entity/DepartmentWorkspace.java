package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class DepartmentWorkspace implements Serializable {
    @Column(name="dept_id", length = 5, nullable = false)
    private String deptId;

    @Column(name="w_id", nullable = false)
    private int wId;
}
