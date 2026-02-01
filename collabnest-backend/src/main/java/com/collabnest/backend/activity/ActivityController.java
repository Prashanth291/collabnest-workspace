package com.collabnest.backend.activity;

import com.collabnest.backend.activity.dto.ActivityLogDto;
import com.collabnest.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogService activityLogService;

    /**
     * Get activity feed for a workspace
     */
    @GetMapping("/workspace/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ActivityLogDto>> getWorkspaceActivities(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDto> activities = activityLogService.getWorkspaceActivities(workspaceId, pageable);
        return ResponseEntity.ok(activities);
    }

    /**
     * Get activities for a specific user in a workspace
     */
    @GetMapping("/workspace/{workspaceId}/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ActivityLogDto>> getUserActivitiesInWorkspace(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDto> activities = activityLogService.getUserActivitiesInWorkspace(workspaceId, userId, pageable);
        return ResponseEntity.ok(activities);
    }

    /**
     * Get my activities across all workspaces
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ActivityLogDto>> getMyActivities(
            @RequestParam UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDto> activities = activityLogService.getUserActivitiesInWorkspace(
                workspaceId, 
                userPrincipal.getUserId(), 
                pageable
        );
        return ResponseEntity.ok(activities);
    }
}
