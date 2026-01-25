package com.collabnest.backend.auth;

import com.collabnest.backend.auth.dto.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register-admin")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AuthResponse registerAdmin(@RequestBody RegisterRequest request) {
        return authService.registerAdmin(request);
    }
}
