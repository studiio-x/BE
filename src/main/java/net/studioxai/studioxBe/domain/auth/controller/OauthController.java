package net.studioxai.studioxBe.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.service.OauthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Slf4j
public class OauthController {

    private final OauthService oauthService;

    @Value("${oauth.google.frontend-redirect-url}")
    private String googleFrontendRedirectUrl;


    // 구글 로그인 요청
    @GetMapping("/v1/oauth/google")
    public ResponseEntity<Void> redirectToGoogleLogin() {
        String googleLoginUrl = oauthService.getGoogleLoginUrl();

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, googleLoginUrl)
                .build();
    }


    // 구글 로그인 콜백(인가 코드 수신)
    @GetMapping("/oauth/google/callback")
    public LoginResponse googleCallback(@RequestParam String code) {

        return oauthService.loginWithGoogle(code);
    }
}
