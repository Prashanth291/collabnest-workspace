package com.collabnest.backend.auth.oauth2;

import java.util.Map;

/**
 * Abstract class to extract user information from different OAuth2 providers.
 * Each provider (Google, GitHub) has its own implementation.
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();
}
