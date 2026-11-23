package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.dto.ActiveDepartmentsWorkspaceDTO;
import com.myapp.worktrack_backend.registration.entity.Department;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkspaceIsInactiveException;
import com.myapp.worktrack_backend.registration.exceptionhandler.WorkspaceNotFound;
import com.myapp.worktrack_backend.registration.repository.DepartmentRepo;
import com.myapp.worktrack_backend.registration.repository.WorkspaceRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentService {
    private final WorkspaceRepo wkspaceRepo;
    private final DepartmentRepo departmentRepo;

    public DepartmentService(WorkspaceRepo wkspaceRepo, DepartmentRepo departmentRepo) {
        this.wkspaceRepo=wkspaceRepo;
        this.departmentRepo=departmentRepo;
    }

    public List<ActiveDepartmentsWorkspaceDTO> getActiveDepartmentsbyWorkspace(int workspaceId) {
        Workspace workspace = wkspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFound("Workspace Not found"));
        String workspaceStatus = workspace.getStatus();
        if(!"Active".equalsIgnoreCase(workspaceStatus)) {
            throw new WorkspaceIsInactiveException("Workspace is  inActive");
        }
        //list od departments within active workspace
            List<Department> departments = departmentRepo.findByWorkspaceAndStatus(workspace, "Active");
        //returns all active departments within the selcted workspace
        List<ActiveDepartmentsWorkspaceDTO> activeDepartments = new ArrayList<>();
        ActiveDepartmentsWorkspaceDTO deptObj;
        for(Department department: departments) {
            deptObj = new ActiveDepartmentsWorkspaceDTO();
            deptObj.setDepartmantName(department.getDeptName());
            deptObj.setWorkspaceName(department.getWorkspace().getWorkspaceName());
            deptObj.setDepartmentId(department.getDepartmentwkspc().getDeptId());
            deptObj.setWorspaceId(department.getWorkspace().getWId());
            activeDepartments.add(deptObj);
        }

        return activeDepartments;
    }
}
