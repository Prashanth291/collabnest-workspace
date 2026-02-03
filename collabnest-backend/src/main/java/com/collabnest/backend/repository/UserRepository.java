package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);
}

