package com.collabnest.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 150, message = "Name must be at most 150 characters") String name,

        @Email(message = "Invalid email format") String email,

        @Size(min = 6, message = "Password must be at least 6 characters") String password) {
}
