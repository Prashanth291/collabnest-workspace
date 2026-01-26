package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.entity.WorkspaceMember;
import com.collabnest.backend.domain.enums.WorkspaceRole;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceMemberRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import com.collabnest.backend.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Override
    public Workspace createWorkspace(String name, UUID ownerId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

    @Override
    public List<Workspace> getUserWorkspaces(UUID userId) {
        return workspaceRepository.findByOwnerId(userId);
    }

    @Override
    public Workspace updateWorkspace(UUID workspaceId, String name) {
        Workspace workspace = getWorkspace(workspaceId);
        workspace.setName(name);
        return workspaceRepository.save(workspace);
    }

    @Override
    public void deleteWorkspace(UUID workspaceId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }
    
    @Override
    public void addMember(UUID workspaceId, UUID userId, WorkspaceRole role) {
        Workspace workspace = getWorkspace(workspaceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if member already exists
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent()) {
            throw new RuntimeException("User is already a member of this workspace");
        }
        
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .isPrimaryOwner(false)
                .build();
        
        workspaceMemberRepository.save(member);
    }
    
    @Override
    public void removeMember(UUID workspaceId, UUID userId) {
        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Prevent removing primary owner
        if (Boolean.TRUE.equals(member.getIsPrimaryOwner())) {
            throw new RuntimeException("Cannot remove primary owner");
        }
        
        workspaceMemberRepository.delete(member);
    }
}
