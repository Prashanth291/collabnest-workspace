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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Workspace createWorkspace(String name, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create workspace
        Workspace workspace = Workspace.builder()
                .name(name)
                .ownerId(ownerId)
                .inviteToken(UUID.randomUUID().toString())
                .build();
        
        workspace = workspaceRepository.save(workspace);
        
        // Automatically add owner as primary owner member
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(owner)
                .role(WorkspaceRole.OWNER)
                .isPrimaryOwner(true)
                .build();
        
        workspaceMemberRepository.save(ownerMember);
        
        return workspace;
    }

    @Override
    public Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

    @Override
    public List<Workspace> getUserWorkspaces(UUID userId) {
        // Get all workspaces where user is a member
        return workspaceMemberRepository.findByUserId(userId)
                .stream()
                .map(WorkspaceMember::getWorkspace)
                .toList();
    }

    @Override
    @Transactional
    public Workspace updateWorkspace(UUID workspaceId, String name) {
        Workspace workspace = getWorkspace(workspaceId);
        workspace.setName(name);
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = getWorkspace(workspaceId);
        
        // Delete all workspace members first (due to FK constraint)
        workspaceMemberRepository.deleteByWorkspaceId(workspaceId);
        
        // Delete workspace
        workspaceRepository.delete(workspace);
    }
    
    @Override
    public String inviteMember(UUID workspaceId, String email, WorkspaceRole role, UUID inviterId) {
        Workspace workspace = getWorkspace(workspaceId);
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Check if user is already a member
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).isPresent()) {
            throw new RuntimeException("User is already a member of this workspace");
        }
        
        // Add member with specified role
        addMember(workspaceId, user.getId(), role);
        
        // Return invite token for notification/email
        return workspace.getInviteToken();
    }
    
    @Override
    @Transactional
    public void joinWorkspace(String inviteToken, UUID userId) {
        // Find workspace by invite token
        Workspace workspace = workspaceRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Invalid invite token"));
        
        // Check if user is already a member
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userId).isPresent()) {
            throw new RuntimeException("You are already a member of this workspace");
        }
        
        // Add user as MEMBER role (default for join via invite)
        addMember(workspace.getId(), userId, WorkspaceRole.MEMBER);
    }
    
    @Override
    @Transactional
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
