package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserTestController {

    private final UserRepository userRepository;

    public UserTestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public List<User> users() {
        return userRepository.findAll();
    }
}

