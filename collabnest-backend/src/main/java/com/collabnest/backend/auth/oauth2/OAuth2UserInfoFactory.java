package com.collabnest.backend.auth.oauth2;

import com.collabnest.backend.domain.enums.AuthProvider;
import com.collabnest.backend.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;

/**
 * Factory to create appropriate OAuth2UserInfo based on provider type.
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
            return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
