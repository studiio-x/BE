package net.studioxai.studioxBe.domain.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.CutoutResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ImageGenerateResponse;
import net.studioxai.studioxBe.domain.image.dto.response.RawPresignResponse;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/raw/presign")
    public ResponseEntity<RawPresignResponse> issueRawPresign(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(imageService.issueRawPresign(principal.getUserId()));
    }

    // 2~3. 검증 후 AI 누끼 → cutout을 S3에 저장 → URL 반환
    @PostMapping("/cutout")
    public ResponseEntity<CutoutResponse> cutout(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody @Valid CutoutRequest request
    ) {
        return ResponseEntity.ok(imageService.cutout(principal.getUserId(), request));
    }


}
