package com.collabnest.backend.security;

import com.collabnest.backend.domain.entity.WorkspaceMember;
import com.collabnest.backend.domain.enums.WorkspaceRole;
import com.collabnest.backend.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Centralized service for workspace-level permission checks.
 * Validates user membership and role-based access to workspace resources.
 */
@Service
public class WorkspacePermissionService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspacePermissionService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    /**
     * Check if a user has ANY access to a workspace (is a member).
     */
    public boolean hasWorkspaceAccess(UUID userId, UUID workspaceId) {
        return workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .isPresent();
    }

    /**
     * Check if a user has at least the specified role in a workspace.
     * Role hierarchy: OWNER > ADMIN > MEMBER > VIEWER
     */
    public boolean hasMinimumRole(UUID userId, UUID workspaceId, WorkspaceRole requiredRole) {
        return workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> hasMinimumRole(member.getRole(), requiredRole))
                .orElse(false);
    }

    /**
     * Check if a user has a specific role in a workspace.
     */
    public boolean hasExactRole(UUID userId, UUID workspaceId, WorkspaceRole role) {
        return workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole() == role)
                .orElse(false);
    }

    /**
     * Check if a user is the workspace owner.
     */
    public boolean isWorkspaceOwner(UUID userId, UUID workspaceId) {
        return hasExactRole(userId, workspaceId, WorkspaceRole.OWNER);
    }

    /**
     * Check if a user is a workspace admin (OWNER or ADMIN).
     */
    public boolean isWorkspaceAdmin(UUID userId, UUID workspaceId) {
        return hasMinimumRole(userId, workspaceId, WorkspaceRole.ADMIN);
    }

    /**
     * Get the user's role in a workspace.
     */
    public WorkspaceRole getUserRole(UUID userId, UUID workspaceId) {
        return workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMember::getRole)
                .orElse(null);
    }

    /**
     * Role hierarchy comparison.
     * Returns true if userRole >= requiredRole.
     */
    private boolean hasMinimumRole(WorkspaceRole userRole, WorkspaceRole requiredRole) {
        int userLevel = getRoleLevel(userRole);
        int requiredLevel = getRoleLevel(requiredRole);
        return userLevel >= requiredLevel;
    }

    /**
     * Convert role to numeric level for comparison.
     * Higher number = more permissions.
     */
    private int getRoleLevel(WorkspaceRole role) {
        return switch (role) {
            case OWNER -> 4;
            case ADMIN -> 3;
            case MEMBER -> 2;
            case VIEWER -> 1;
        };
    }
}
