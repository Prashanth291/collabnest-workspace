package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.UserRole;
import com.collabnest.backend.dto.user.UserResponse;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> adminDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of(
                "message", "Welcome to Admin Dashboard",
                "userId", principal.getUserId(),
                "username", principal.getUsername(),
                "role", principal.getUser().getRole(),
                "authorities", principal.getAuthorities());
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return toUserResponse(userService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public UserResponse changeUserRole(@PathVariable UUID id, @RequestParam UserRole role) {
        return toUserResponse(userService.changeUserRole(id, role));
    }

    @PutMapping("/users/{id}/enable")
    public UserResponse enableUser(@PathVariable UUID id, @RequestParam boolean enabled) {
        return toUserResponse(userService.enableUser(id, enabled));
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return Map.of("message", "User deleted successfully");
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
