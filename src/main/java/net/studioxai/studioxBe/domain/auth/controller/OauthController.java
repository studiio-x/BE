package net.studioxai.studioxBe.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.GoogleCallbackDto;
import net.studioxai.studioxBe.domain.auth.service.OauthService;
import net.studioxai.studioxBe.global.util.CookieUtil;
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
    private final CookieUtil cookieUtil;

    // 구글 로그인 요청
    @GetMapping("/v1/oauth/google")
    public ResponseEntity<Void> redirectToGoogleLogin(@RequestParam String redirectUrl) {
        String googleLoginUrl = oauthService.getGoogleLoginUrl(redirectUrl);

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, googleLoginUrl)
                .build();
    }


    // 구글 로그인 콜백(인가 코드 수신)
    @GetMapping("/oauth/google/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam String code, @RequestParam String state) {

        GoogleCallbackDto googleCallbackDto = oauthService.loginWithGoogle(code, state);

        ResponseCookie refreshCookie = cookieUtil.getRefreshTokenCookie(googleCallbackDto.refreshToken());
        ResponseCookie accessTokenCookie = cookieUtil.getAccessTokenCookie(googleCallbackDto.accessToken());

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, googleCallbackDto.redirectUrl())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

}
