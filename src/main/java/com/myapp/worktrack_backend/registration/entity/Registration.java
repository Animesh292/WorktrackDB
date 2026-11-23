package com.myapp.worktrack_backend.registration.entity;

import com.myapp.worktrack_backend.registration.auth.AuthorizableResource;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Entity
@Table(name="registration")
public class Registration implements AuthorizableResource {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="reg_id")
    private int regId;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="work_email")
    private String workEmail;

    @Column(name="sex")
    private char sex;

    @Column(name="phn_num")
    private String phnNum;

    @Column(name="workspace_id", nullable = false)
    private Integer workspaceId;

    @Column(name="department_id", nullable = false, length = 5)
    private String departmentId;

    @Column(name="experience")
    private float experience;

    @Column(name="birthdate")
    private LocalDate date;

    @Column(name="role")
    private String role;

    @Column(name="application_status")
    private String applicationStatus;

    @Column(name="registration_uuid")
    private String registrationUUID;

    public void setRegID(int regid) {
        this.regId=regid;
    }
    public int getRegID() {
        return this.regId;
    }
    public void setWorkEmail(String workemail) {
        this.workEmail=workemail;
    }
    public String getWorkEmail() {
        return this.workEmail;
    }

    public void setFirstName(String firstName) {
        this.firstName=firstName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setLastName(String lastName) {
        this.lastName=lastName;
    }
    public String getLastName() {
         return this.lastName;
    }

    public void setExperience(float exp) {
        this.experience=exp;
    }

    public float getExperience() {
        return this.experience;
    }

    public void setDepartmentId(String deptId) {
        this.departmentId=deptId;
    }

    public String getDepartmentId() {
        return this.departmentId;
    }

    public void setWorkspaceId(Integer wkspcId) {
        this.workspaceId=wkspcId;
    }

    public Integer getWorkspaceId() {
        return this.workspaceId;
    }

    public String getPhnNum() {
        return this.phnNum;
    }

    public void setPhnNum(String phnNum) {
        this.phnNum=phnNum;
    }

    public char getSex() {
        return this.sex;
    }

    public void setSex(char sex) {
        this.sex=sex;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.date=birthdate;
    }

    public LocalDate getBirthdate() {
        return this.date;
    }

    public void setRole(String role) {
        this.role=role;
    }

    public String getRole() {
        return this.role;
    }

    public void setApplicationStatus(String status) {
        this.applicationStatus=status;
    }

    public String getApplicationStatus() {
        return this.applicationStatus;
    }

    public void setRegistrationUUID(String reg_uuid) {
        this.registrationUUID=reg_uuid;
    }

    public String getRegistrationUUID() {
        return this.registrationUUID;
    }
}



