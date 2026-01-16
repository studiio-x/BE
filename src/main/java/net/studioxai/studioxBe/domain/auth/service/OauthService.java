package net.studioxai.studioxBe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleUserInfoResponse;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class OauthService {

    private final GoogleOauth googleOauth;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Value("${server.front-urls}")
    private String[] FRONT_URLS;

    public String getGoogleLoginUrl(String redirectUrl) {
        validateRedirectUrl(redirectUrl);
        return googleOauth.getOauthRedirectURL(redirectUrl);
    }

    public String loginWithGoogle(String code, String redirectUrl) {
        validateCode(code);
        validateRedirectUrl(redirectUrl);

        GoogleUserInfoResponse userInfo = getGoogleUserInfo(code);
        User user = findOrCreateGoogleUser(userInfo);

        Map<String, String> tokens = authService.issueTokens(user.getId());

        return redirectUrl +
                "?accessToken=" + tokens.get("accessToken") +
                "&refreshToken=" + tokens.get("refreshToken");
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new AuthExceptionHandler(AuthErrorCode.GOOGLE_AUTH_CODE_MISSING);
        }
    }

    private void validateRedirectUrl(String redirectUrl) {
        boolean allowed = Arrays.stream(FRONT_URLS)
                .anyMatch(redirectUrl::startsWith);

        if (!allowed) {
            throw new AuthExceptionHandler(AuthErrorCode.INVALID_REDIRECT_URL);
        }
    }

    private GoogleUserInfoResponse getGoogleUserInfo(String code) {
        GoogleTokenResponse tokenResponse = googleOauth.requestAccessToken(code);

        return googleOauth.requestUserInfo(tokenResponse.accessToken());
    }

    private User findOrCreateGoogleUser(GoogleUserInfoResponse userInfo) {
        return userRepository.findByGoogleSub(userInfo.sub())
                .orElseGet(() -> userRepository.save(
                        User.createGoogleUser(
                                userInfo.sub(),
                                userInfo.email(),
                                userInfo.name(),
                                passwordEncoder.encode(UUID.randomUUID().toString()),
                                resolveProfileImage(userInfo)
                        )
                ));
    }

    private String resolveProfileImage(GoogleUserInfoResponse userInfo) {
        if (userInfo.profileImage() == null || userInfo.profileImage().isBlank()) {
            return AuthService.DEFAULT_PROFILE_IMAGE_URL;
        }
        return userInfo.profileImage();
    }

}
