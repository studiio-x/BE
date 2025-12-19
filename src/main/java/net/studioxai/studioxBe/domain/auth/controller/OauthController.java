package net.studioxai.studioxBe.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginTokenResult;
import net.studioxai.studioxBe.domain.auth.service.OauthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Slf4j
public class OauthController {

    private final OauthService oauthService;

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
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {

        LoginTokenResult tokenResult = oauthService.handleGoogleLogin(code);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResult.getAccessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResult.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 14)
                .build();

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.LOCATION, "http://localhost:3000")
                .build();
    }
}
