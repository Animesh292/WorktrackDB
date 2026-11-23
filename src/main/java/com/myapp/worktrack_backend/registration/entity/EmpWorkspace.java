package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="emp_workspace")
public class EmpWorkspace {

    @EmbeddedId
    private EmpWorkspaceId empWorkspaceId;
    @Column(name="dept_id", length = 5)
    private String deptId;

}
