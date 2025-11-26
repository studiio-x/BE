package net.studioxai.studioxBe.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.auth.dto.request.LoginRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.user.dto.MypageResponse;
import net.studioxai.studioxBe.domain.user.service.UserService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MypageController {
    private final UserService userService;

    @GetMapping("/v1/mypage")
    public MypageResponse userDetails(
            @AuthenticationPrincipal JwtUserPrincipal principal
            ) {
        return userService.findUserDetail(principal.userId());
    }

}
