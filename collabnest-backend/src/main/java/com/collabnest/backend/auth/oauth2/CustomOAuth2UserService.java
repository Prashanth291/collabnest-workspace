package com.collabnest.backend.auth.oauth2;

import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.AuthProvider;
import com.collabnest.backend.domain.enums.UserRole;
import com.collabnest.backend.exception.OAuth2AuthenticationProcessingException;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Custom OAuth2UserService to handle user registration/login after OAuth2 authentication.
 * Integrates with Google and GitHub OAuth2 providers.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        logger.info("OAuth2 Login Attempt - Provider: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId());
        logger.info("OAuth2 User Attributes: {}", oAuth2User.getAttributes());

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            logger.error("OAuth2 Authentication failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("OAuth2 Processing error: {}", ex.getMessage(), ex);
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        
        // For GitHub, fetch email from /user/emails endpoint if not in attributes
        if ("github".equals(registrationId) && oAuth2User.getAttribute("email") == null) {
            logger.info("GitHub email is null, fetching from /user/emails endpoint");
            String email = fetchGitHubEmail(oAuth2UserRequest);
            if (email != null) {
                // Add email to attributes
                java.util.Map<String, Object> attributes = new java.util.HashMap<>(oAuth2User.getAttributes());
                attributes.put("email", email);
                oAuth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    oAuth2User.getAuthorities(), 
                    attributes, 
                    "login"
                );
                logger.info("Successfully fetched GitHub email: {}", email);
            }
        }
        
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        logger.info("Extracted email: {}, name: {}, id: {}", oAuth2UserInfo.getEmail(), oAuth2UserInfo.getName(), oAuth2UserInfo.getId());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            logger.error("Email not found from OAuth2 provider: {}", registrationId);
            throw new OAuth2AuthenticationProcessingException(
                "Email not found from OAuth2 provider. Please make sure your email is public in your " + 
                registrationId.toUpperCase() + " account settings, or grant email access permission."
            );
        }

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        Optional<User> userOptional = userRepository.findByAuthProviderAndProviderId(authProvider, oAuth2UserInfo.getId());

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

        // Check if email already exists with different provider
        Optional<User> existingUser = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        if (existingUser.isPresent()) {
            throw new OAuth2AuthenticationProcessingException(
                    "Email already registered with " + existingUser.get().getAuthProvider() + 
                    ". Please use " + existingUser.get().getAuthProvider() + " to login."
            );
        }

        User user = new User();
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setName(oAuth2UserInfo.getName());
        user.setAuthProvider(authProvider);
        user.setProviderId(oAuth2UserInfo.getId());
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        
        // Generate username from email (before @)
        String username = oAuth2UserInfo.getEmail().split("@")[0];
        // Ensure username is unique
        int counter = 1;
        String originalUsername = username;
        while (userRepository.findByUsername(username).isPresent()) {
            username = originalUsername + counter;
            counter++;
        }
        user.setUsername(username);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        return userRepository.save(existingUser);
    }
    
    private String fetchGitHubEmail(OAuth2UserRequest oAuth2UserRequest) {
        try {
            String emailsUrl = "https://api.github.com/user/emails";
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(oAuth2UserRequest.getAccessToken().getTokenValue());
            
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<java.util.List> response = restTemplate.exchange(
                emailsUrl,
                org.springframework.http.HttpMethod.GET,
                entity,
                java.util.List.class
            );
            
            if (response.getBody() != null && !response.getBody().isEmpty()) {
                // Find the primary verified email
                for (Object emailObj : response.getBody()) {
                    if (emailObj instanceof java.util.Map) {
                        java.util.Map<String, Object> emailData = (java.util.Map<String, Object>) emailObj;
                        Boolean primary = (Boolean) emailData.get("primary");
                        Boolean verified = (Boolean) emailData.get("verified");
                        if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                            return (String) emailData.get("email");
                        }
                    }
                }
                // If no primary verified email, get the first verified one
                for (Object emailObj : response.getBody()) {
                    if (emailObj instanceof java.util.Map) {
                        java.util.Map<String, Object> emailData = (java.util.Map<String, Object>) emailObj;
                        Boolean verified = (Boolean) emailData.get("verified");
                        if (Boolean.TRUE.equals(verified)) {
                            return (String) emailData.get("email");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch GitHub email: {}", e.getMessage());
        }
        return null;
    }
}
