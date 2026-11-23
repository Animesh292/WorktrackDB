package com.myapp.worktrack_backend.registration.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="workspace")
public class Workspace {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "w_id")
    private int wId;

    @Column(name="workspace_name")
    private String workspaceName;

    @Column(name="city")
    private String city;

    @Column(name="state")
    private String state;

    @Column(name="country")
    private String country;

    @Column(name="address")
    private String address;

    @Column(name="headquarters_name")
    private String headquartersName;

    @Column(name="status")
    private String status;
}
