package com.collabnest.backend.auth.dto;

public record LoginRequest(
        String identifier,  // can be email OR username
        String password
) {}
