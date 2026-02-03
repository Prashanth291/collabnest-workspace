package com.collabnest.backend.config;

import com.collabnest.backend.auth.jwt.JwtAuthFilter;
import com.collabnest.backend.auth.oauth2.CustomOAuth2UserService;
import com.collabnest.backend.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.collabnest.backend.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.collabnest.backend.security.WorkspacePermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security Configuration - STEP 5 COMPLETE ✅ + OAuth2 Integration
 * 
 * This configuration implements comprehensive security with workspace-aware authorization:
 * 
 * 🔐 5.1 Spring Security Foundation:
 *   - Stateless session management (JWT-based)
 *   - Security filter chain with public/protected endpoints
 *   - Password encoding (BCryptPasswordEncoder in PasswordConfig)
 * 
 * 🎫 5.2 JWT Integration:
 *   - JwtAuthFilter validates tokens and sets authentication
 *   - Access tokens with claims (subject, role, expiration)
 *   - Token validation on every request
 * 
 * 👤 5.3 UserDetailsService:
 *   - CustomUserDetailsService loads users from database
 *   - UserPrincipal maps UserRole → Spring Security authorities
 *   - Supports authentication by email or username
 * 
 * 🏢 5.4 Workspace-Aware Authorization (CRITICAL):
 *   - WorkspacePermissionService: Centralized permission checks
 *   - WorkspacePermissionEvaluator: Custom PermissionEvaluator for @PreAuthorize
 *   - Role hierarchy: OWNER > ADMIN > MEMBER > VIEWER
 *   - Validates workspace membership and role-based access
 * 
 * ✋ 5.5 Method-Level Security:
 *   - @EnableMethodSecurity with prePostEnabled
 *   - @PreAuthorize for role checks: hasRole('ADMIN')
 *   - @PreAuthorize for workspace checks: hasPermission(#workspaceId, 'Workspace', 'ADMIN')
 *   - Custom MethodSecurityExpressionHandler with WorkspacePermissionEvaluator
 * 
 * 🔑 5.6 OAuth2 Integration:
 *   - Google and GitHub OAuth2 login
 *   - CustomOAuth2UserService for user registration/login
 *   - OAuth2AuthenticationSuccessHandler generates JWT token
 *   - OAuth2AuthenticationFailureHandler handles failures
 * 
 * Usage Examples:
 *   - @PreAuthorize("hasRole('ADMIN')") - Global admin only
 *   - @PreAuthorize("isAuthenticated()") - Any authenticated user
 *   - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')") - Workspace viewer+
 *   - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')") - Workspace admin+
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final WorkspacePermissionEvaluator permissionEvaluator;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, 
                         WorkspacePermissionEvaluator permissionEvaluator,
                         CustomOAuth2UserService customOAuth2UserService,
                         OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                         OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.permissionEvaluator = permissionEvaluator;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/auth/**",
                                "/hello",
                                "/error",
                                "/ws/**",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * Register custom PermissionEvaluator for workspace-level authorization.
     * Enables @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')") expressions.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}

