package com.collabnest.backend.dto.user;

import com.collabnest.backend.domain.enums.AuthProvider;
import com.collabnest.backend.domain.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String name,
        AuthProvider authProvider,
        UserRole role,
        Boolean enabled,
        Instant createdAt) {
}
