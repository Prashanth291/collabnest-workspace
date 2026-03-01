package com.collabnest.backend.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles OAuth2 authentication failures.
 * Redirects to frontend with error message, sanitizing any newline characters
 * that would cause an invalid HTTP header.
 */
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = exception.getMessage();

        // Sanitize the error message to remove any newlines or special characters
        if (errorMessage != null) {
            errorMessage = errorMessage.replaceAll("[\\r\\n]", " ");
        } else {
            errorMessage = "Authentication failed";
        }

        // Encode the error message for URL safety
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // Derive the base origin from the redirect URI (strip path like /dashboard)
        String baseUrl;
        try {
            java.net.URI uri = java.net.URI.create(redirectUri);
            baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        } catch (Exception e) {
            baseUrl = "http://localhost:3000";
        }

        // Redirect to login page with error parameter
        String redirectUrl = baseUrl + "/login?error=true&message=" + encodedError;

        response.sendRedirect(redirectUrl);
    }
}
