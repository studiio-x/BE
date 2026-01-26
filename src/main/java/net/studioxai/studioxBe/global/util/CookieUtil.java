package net.studioxai.studioxBe.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtProperties jwtProperties;

    public ResponseCookie getRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(
                        REFRESH_TOKEN_COOKIE_NAME, refreshToken
                )
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.refreshTokenExpirationMs()))
                .build();
    }

    public ResponseCookie getAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(
                        ACCESS_TOKEN_COOKIE_NAME, accessToken
                )
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.accessTokenExpirationMs()))
                .build();
    }

    public String getAccessTokenValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getRrefreshTokenValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new AuthExceptionHandler(AuthErrorCode.NO_TOKEN);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AuthExceptionHandler(AuthErrorCode.NO_TOKEN));
    }
}
