package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.WorkspaceRole;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    Workspace createWorkspace(String name, UUID ownerId);

    Workspace getWorkspace(UUID workspaceId);
    
    Workspace updateWorkspace(UUID workspaceId, String name);

    List<Workspace> getUserWorkspaces(UUID userId);

    void deleteWorkspace(UUID workspaceId);
    
    String inviteMember(UUID workspaceId, String email, WorkspaceRole role, UUID inviterId);
    
    void joinWorkspace(String inviteToken, UUID userId);
    
    void addMember(UUID workspaceId, UUID userId, WorkspaceRole role);
    
    void removeMember(UUID workspaceId, UUID userId);
}
