package com.collabnest.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> getProfile(Authentication auth) {
        return Map.of(
                "message", "User profile",
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> userDashboard(Authentication auth) {
        return Map.of(
                "message", "Welcome to Dashboard",
                "user", auth.getName()
        );
    }
}
