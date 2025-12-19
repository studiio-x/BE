package net.studioxai.studioxBe.domain.auth.service;

import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;

public interface SocialOauth {
    String getOauthRedirectURL();
    GoogleTokenResponse requestAccessToken(String code);
}
