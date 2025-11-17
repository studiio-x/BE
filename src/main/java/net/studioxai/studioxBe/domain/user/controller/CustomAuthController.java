package net.studioxai.studioxBe.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.user.dto.LoginRequest;
import net.studioxai.studioxBe.domain.user.dto.LoginResponse;
import net.studioxai.studioxBe.domain.user.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomAuthController {
    private final AuthService authService;

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

}
