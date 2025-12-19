package net.studioxai.studioxBe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleUserInfoResponse;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOauth implements SocialOauth {

    @Value("${sns.google.url}")
    private String GOOGLE_SNS_BASE_URL;

    @Value("${sns.google.client.id}")
    private String GOOGLE_SNS_CLIENT_ID;

    @Value("${sns.google.callback.url}")
    private String GOOGLE_SNS_CALLBACK_URL;

    @Value("${sns.google.client.secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;

    @Value("${sns.google.token.url}")
    private String GOOGLE_SNS_TOKEN_BASE_URL;

    @Value("${sns.google.userinfo.url}")
    private String GOOGLE_SNS_USERINFO_URL;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", "profile email");
        params.put("response_type", "code");
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);

        String parameterString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return GOOGLE_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public GoogleTokenResponse requestAccessToken(String code) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("client_secret", GOOGLE_SNS_CLIENT_SECRET);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);
        params.put("grant_type", "authorization_code");

        ResponseEntity<GoogleTokenResponse> response =
                restTemplate.postForEntity(
                        GOOGLE_SNS_TOKEN_BASE_URL,
                        params,
                        GoogleTokenResponse.class
                );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AuthExceptionHandler(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        return response.getBody();
    }


    public GoogleUserInfoResponse requestUserInfo(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfoResponse> response =
                restTemplate.exchange(
                        GOOGLE_SNS_USERINFO_URL,
                        HttpMethod.GET,
                        request,
                        GoogleUserInfoResponse.class
                );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AuthExceptionHandler(AuthErrorCode.GOOGLE_USER_INFO_REQUEST_FAILED);
        }

        return response.getBody();
    }


}
