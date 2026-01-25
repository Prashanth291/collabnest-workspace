package com.collabnest.backend.security;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + identifier
                        )
                );

        return new UserPrincipal(user);
    }
}
