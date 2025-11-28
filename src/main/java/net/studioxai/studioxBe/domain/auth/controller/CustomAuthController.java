package net.studioxai.studioxBe.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.auth.dto.request.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.LoginRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.TokenResponse;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.auth.service.EmailVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomAuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/v1/auth/login")
    public LoginResponse login(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        return authService.login(loginRequest);
    }

    @PostMapping("/v1/auth/signup")
    public LoginResponse signup(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        return authService.signUp(loginRequest);
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
    public TokenResponse tokenReissue(
            @RequestParam String refreshToken
    ) {
        return authService.reissue(refreshToken);
    }

}
