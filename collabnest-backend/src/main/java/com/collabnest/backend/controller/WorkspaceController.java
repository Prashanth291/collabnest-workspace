package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.WorkspaceRole;
import com.collabnest.backend.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Workspace controller demonstrating workspace-level authorization.
 * 
 * Permission levels:
 * - VIEWER: Can view workspace and resources
 * - MEMBER: Can create/edit own resources
 * - ADMIN: Can manage workspace settings and members
 * - OWNER: Full control, can delete workspace
 */
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * Create a new workspace - any authenticated user can create.
     * TODO: Implement proper user ID extraction in Step 6
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Workspace> createWorkspace(
            @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // For now, using placeholder - will be implemented in Step 6
        throw new UnsupportedOperationException("Create workspace will be implemented in Step 6");
    }

    /**
     * Get workspace details - requires at least VIEWER access.
     */
    @GetMapping("/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable UUID workspaceId) {
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        return ResponseEntity.ok(workspace);
    }

    /**
     * Update workspace - requires ADMIN or OWNER.
     */
    @PutMapping("/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Workspace> updateWorkspace(
            @PathVariable UUID workspaceId,
            @RequestBody UpdateWorkspaceRequest request) {
        
        Workspace workspace = workspaceService.updateWorkspace(
                workspaceId, 
                request.getName()
        );
        return ResponseEntity.ok(workspace);
    }

    /**
     * Delete workspace - requires OWNER only.
     */
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'OWNER')")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * List all workspaces the user has access to.
     * TODO: Implement proper user ID extraction in Step 6
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Workspace>> listMyWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // For now, using placeholder - will be implemented in Step 6
        throw new UnsupportedOperationException("List workspaces will be implemented in Step 6");
    }

    /**
     * Add member to workspace - requires ADMIN or OWNER.
     */
    @PostMapping("/{workspaceId}/members")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID workspaceId,
            @RequestBody AddMemberRequest request) {
        
        WorkspaceRole role = WorkspaceRole.valueOf(request.getRole().toUpperCase());
        workspaceService.addMember(
                workspaceId, 
                request.getUserId(), 
                role
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Remove member from workspace - requires ADMIN or OWNER.
     */
    @DeleteMapping("/{workspaceId}/members/{userId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId) {
        
        workspaceService.removeMember(workspaceId, userId);
        return ResponseEntity.noContent().build();
    }

    // DTO classes
    public static class CreateWorkspaceRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class UpdateWorkspaceRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class AddMemberRequest {
        private UUID userId;
        private String role;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
