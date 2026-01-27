package net.studioxai.studioxBe.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.request.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.LoginRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.TokenResponse;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.auth.service.EmailVerificationService;
import net.studioxai.studioxBe.global.util.CookieUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CustomAuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final CookieUtil cookieUtil;

    @PostMapping("/v1/auth/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        LoginResponse response = authService.login(loginRequest);

        ResponseCookie refreshCookie = cookieUtil.getRefreshTokenCookie(response.refreshToken());
        ResponseCookie accessTokenCookie = cookieUtil.getAccessTokenCookie(response.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString())
                .body(response.withoutTokens());
    }

    @PostMapping("/v1/auth/signup")
    public ResponseEntity<LoginResponse> signup(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        LoginResponse response = authService.signUp(loginRequest);

        ResponseCookie refreshCookie = cookieUtil.getRefreshTokenCookie(response.refreshToken());
        ResponseCookie accessTokenCookie = cookieUtil.getAccessTokenCookie(response.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString())
                .body(response.withoutTokens());
    }

    @PostMapping("/v1/auth/email/verification")
    public void emailSend(
            @RequestBody @Valid EmailVerificationRequest emailVerificationRequest
    ) {
        String currentUrl = "/api/v1/auth/email/verification";
        emailVerificationService.sendEmail(emailVerificationRequest, currentUrl);
    }

    @GetMapping("/v1/auth/email/verification")
    public ResponseEntity<Void> emailVerify(
            @RequestParam String email,
            @RequestParam String token
    ) {
        String callbackUrl = emailVerificationService.verifyEmail(email, token);

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header("Location", callbackUrl)
                .build();
    }

    @PostMapping("/v1/auth/token")
    public ResponseEntity<Void> tokenReissue(
            HttpServletRequest request
    ) {
        String refreshToken = cookieUtil.getRrefreshTokenValue(request);
        TokenResponse response = authService.reissue(refreshToken);

        ResponseCookie refreshCookie = cookieUtil.getRefreshTokenCookie(response.refreshToken());
        ResponseCookie accessTokenCookie = cookieUtil.getAccessTokenCookie(response.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

}
