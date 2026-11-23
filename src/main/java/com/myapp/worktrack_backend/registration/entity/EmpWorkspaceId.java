package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class EmpWorkspaceId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name="emp_id", length = 50)
    private String empId;
    @Column(name="work_id")
    private int workSpaceId;

}
