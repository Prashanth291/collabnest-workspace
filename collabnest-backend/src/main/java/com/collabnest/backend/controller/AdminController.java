package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.UserRole;
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
                "authorities", principal.getAuthorities()
        );
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}/role")
    public User changeUserRole(@PathVariable UUID id, @RequestParam UserRole role) {
        return userService.changeUserRole(id, role);
    }

    @PutMapping("/users/{id}/enable")
    public User enableUser(@PathVariable UUID id, @RequestParam boolean enabled) {
        return userService.enableUser(id, enabled);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return Map.of("message", "User deleted successfully");
    }
}
