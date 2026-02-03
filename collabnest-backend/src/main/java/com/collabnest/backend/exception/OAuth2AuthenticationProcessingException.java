package com.collabnest.backend.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception for OAuth2 authentication processing errors.
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }

    public OAuth2AuthenticationProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
