package net.studioxai.studioxBe.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.user.dto.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.user.dto.LoginRequest;
import net.studioxai.studioxBe.domain.user.dto.LoginResponse;
import net.studioxai.studioxBe.domain.user.service.AuthService;
import net.studioxai.studioxBe.domain.user.service.EmailVerificationService;
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
    public void emailVerification(
            @RequestBody @Valid EmailVerificationRequest emailVerificationRequest
    ) {
        String currentUrl = "/api/v1/auth/email/verification";
        emailVerificationService.sendEmail(emailVerificationRequest, currentUrl);
    }

}
