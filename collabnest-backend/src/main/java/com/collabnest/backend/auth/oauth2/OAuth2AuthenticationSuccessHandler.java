package com.collabnest.backend.auth.oauth2;

import com.collabnest.backend.auth.jwt.JwtService;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.exception.OAuth2AuthenticationProcessingException;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles successful OAuth2 authentication.
 * Generates JWT token and redirects to frontend with token.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        
        Object principal = authentication.getPrincipal();
        User user;
        
        // Handle both UserPrincipal (from our custom service) and OAuth2User/OidcUser (from Spring)
        if (principal instanceof UserPrincipal) {
            user = ((UserPrincipal) principal).getUser();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            // If it's a default OAuth2User, we need to get the user from our custom service
            // This shouldn't happen if CustomOAuth2UserService is configured correctly,
            // but let's handle it gracefully
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = 
                (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            if (email == null) {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }
            
            // Find or create user
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OAuth2AuthenticationProcessingException("User not found"));
        } else {
            throw new OAuth2AuthenticationProcessingException("Unsupported principal type: " + principal.getClass());
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(user.getEmail(), extraClaims);

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .encode()
                .build()
                .toUriString();
    }
}
