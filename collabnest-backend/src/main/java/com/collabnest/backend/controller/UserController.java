package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.dto.user.UpdateProfileRequest;
import com.collabnest.backend.dto.user.UserResponse;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getUserId());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(
                userPrincipal.getUserId(),
                request.name(),
                request.email());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> userDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getUserId());
        return ResponseEntity.ok(toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getAuthProvider(),
                user.getRole(),
                user.getEnabled(),
                user.getCreatedAt());
    }
}
