package com.collabnest.backend.security;

import com.collabnest.backend.domain.enums.WorkspaceRole;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * Custom PermissionEvaluator for workspace-level authorization.
 * Enables @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')") style expressions.
 * 
 * Usage examples:
 * - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')") // requires at least MEMBER
 * - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')") // requires ADMIN or OWNER
 * - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'OWNER')") // requires OWNER only
 */
@Component
public class WorkspacePermissionEvaluator implements PermissionEvaluator {

    private final WorkspacePermissionService permissionService;

    public WorkspacePermissionEvaluator(WorkspacePermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Evaluate permission: hasPermission(targetId, targetType, permission)
     * 
     * @param authentication Current user authentication
     * @param targetDomainObject The workspace ID (UUID or String)
     * @param targetType Should be "Workspace" or "WORKSPACE"
     * @param permission The required role: "VIEWER", "MEMBER", "ADMIN", or "OWNER"
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        UUID userId = extractUserId(authentication);
        UUID workspaceId = extractWorkspaceId(targetDomainObject);
        
        if (userId == null || workspaceId == null) {
            return false;
        }

        // Convert permission string to WorkspaceRole
        String permissionStr = permission.toString().toUpperCase();
        
        try {
            WorkspaceRole requiredRole = WorkspaceRole.valueOf(permissionStr);
            return permissionService.hasMinimumRole(userId, workspaceId, requiredRole);
        } catch (IllegalArgumentException e) {
            // Invalid role name
            return false;
        }
    }

    /**
     * Evaluate permission: hasPermission(targetId, targetType, permission)
     * This overload is used when targetId is Serializable.
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        
        return hasPermission(authentication, targetId, permission);
    }

    /**
     * Extract user ID from authentication principal.
     * Supports both UserPrincipal and String (from JWT filter).
     */
    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        }
        
        // Fallback: try to parse principal as string (username/email)
        // This happens when JWT filter sets simple string as principal
        if (principal instanceof String principalStr) {
            try {
                return UUID.fromString(principalStr);
            } catch (IllegalArgumentException e) {
                // Not a UUID, might be username/email
                // You could look up user by username here if needed
                return null;
            }
        }
        
        return null;
    }

    /**
     * Extract workspace ID from various input types.
     */
    private UUID extractWorkspaceId(Object target) {
        if (target instanceof UUID uuid) {
            return uuid;
        }
        
        if (target instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        return null;
    }
}
