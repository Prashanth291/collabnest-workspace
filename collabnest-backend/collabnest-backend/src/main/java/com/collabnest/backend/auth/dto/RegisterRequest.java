package com.collabnest.backend.auth.dto;

public record RegisterRequest(
        String email,
        String username,
        String name,
        String password
) {}

