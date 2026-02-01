package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.WorkspaceRole;
import com.collabnest.backend.dto.workspace.CreateWorkspaceRequest;
import com.collabnest.backend.dto.workspace.InviteMemberRequest;
import com.collabnest.backend.dto.workspace.JoinWorkspaceRequest;
import com.collabnest.backend.dto.workspace.WorkspaceResponse;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.security.WorkspacePermissionService;
import com.collabnest.backend.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Workspace controller with workspace-level authorization.
 */
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final WorkspacePermissionService permissionService;

    public WorkspaceController(WorkspaceService workspaceService, WorkspacePermissionService permissionService) {
        this.workspaceService = workspaceService;
        this.permissionService = permissionService;
    }

    /**
     * Create a new workspace.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        Workspace workspace = workspaceService.createWorkspace(request.name(), userId);
        
        WorkspaceResponse response = new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwnerId(),
                WorkspaceRole.OWNER,
                workspace.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get workspace details - requires at least VIEWER access.
     */
    @GetMapping("/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        WorkspaceRole myRole = permissionService.getUserRole(userId, workspaceId);
        
        WorkspaceResponse response = new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwnerId(),
                myRole,
                workspace.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * List all workspaces the user has access to.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkspaceResponse>> listMyWorkspaces(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        List<Workspace> workspaces = workspaceService.getUserWorkspaces(userId);
        
        List<WorkspaceResponse> responses = workspaces.stream()
                .map(workspace -> {
                    WorkspaceRole myRole = permissionService.getUserRole(userId, workspace.getId());
                    return new WorkspaceResponse(
                            workspace.getId(),
                            workspace.getName(),
                            workspace.getOwnerId(),
                            myRole,
                            workspace.getCreatedAt()
                    );
                })
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update workspace - requires ADMIN or OWNER.
     */
    @PutMapping("/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        Workspace workspace = workspaceService.updateWorkspace(workspaceId, request.name());
        WorkspaceRole myRole = permissionService.getUserRole(userId, workspaceId);
        
        WorkspaceResponse response = new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwnerId(),
                myRole,
                workspace.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
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
     * Invite member to workspace by email - requires ADMIN or OWNER.
     */
    @PostMapping("/{workspaceId}/invite")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<String> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID inviterId = userPrincipal.getUserId();
        String inviteToken = workspaceService.inviteMember(
                workspaceId, 
                request.email(), 
                request.role(), 
                inviterId
        );
        
        return ResponseEntity.ok(inviteToken);
    }

    /**
     * Join workspace using invite token.
     */
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> joinWorkspace(
            @Valid @RequestBody JoinWorkspaceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        workspaceService.joinWorkspace(request.inviteToken(), userId);
        
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
}
