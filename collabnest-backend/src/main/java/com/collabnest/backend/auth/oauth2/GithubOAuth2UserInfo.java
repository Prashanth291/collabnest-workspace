package com.collabnest.backend.auth.oauth2;

import java.util.Map;

/**
 * GitHub OAuth2 user information extractor.
 * Maps GitHub's user profile attributes to our application's user model.
 */
public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        // Fallback to login (username) if name is not set
        return name != null ? name : (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
