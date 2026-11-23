package com.myapp.worktrack_backend.registration.service;

import com.myapp.worktrack_backend.registration.dto.ActiveWorkspaceResponseDTO;
import com.myapp.worktrack_backend.registration.entity.Workspace;
import com.myapp.worktrack_backend.registration.repository.WorkspaceRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkspaceService {
    private final WorkspaceRepo workspaceRepo;

    public WorkspaceService(WorkspaceRepo workspaceRepo) {
        this.workspaceRepo=workspaceRepo;
    }

    public List<ActiveWorkspaceResponseDTO> getActiveWorkspaces() {
        List<Workspace> activeWorkspaces= workspaceRepo.findByStatusIgnoreCase("Active");
        List<ActiveWorkspaceResponseDTO> ans = new ArrayList<>();
        for(Workspace workspace: activeWorkspaces) {
            ActiveWorkspaceResponseDTO awrdto = new ActiveWorkspaceResponseDTO();

            awrdto.setWorkspaceId(workspace.getWId());
            awrdto.setWorkspaceName(workspace.getWorkspaceName());
            ans.add(awrdto);
        }
        return ans;
    }
}
