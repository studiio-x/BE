package net.studioxai.studioxBe.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.user.dto.request.ProfileUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.request.UsernameUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.response.MypageResponse;
import net.studioxai.studioxBe.domain.user.service.UserService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlGenerator;
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

    @PutMapping("/v1/mypage/profile")
    public void profileModify(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ) {
        userService.updateUserProfile(principal.userId(), profileUpdateRequest);
    }

    @PutMapping("/v1/mypage/username")
    public void usernameModify(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody @Valid UsernameUpdateRequest usernameUpdateRequest
    ) {
        userService.updateUsername(principal.userId(), usernameUpdateRequest);
    }

    @GetMapping("/v1/mypage/profile")
    public S3Url profileImageUrl() {
        return userService.getProfileImageUrl();
    }

}
