package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.UserRole;
import com.collabnest.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User changeUserRole(UUID userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public User enableUser(UUID userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }
}
