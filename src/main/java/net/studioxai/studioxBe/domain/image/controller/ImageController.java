package net.studioxai.studioxBe.domain.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
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

    @GetMapping("/v1/image/raw/presign")
    public ResponseEntity<PresignResponse> issuePresign() {

        return ResponseEntity.ok(imageService.issuePresign());
    }

    @PostMapping("/v1/image/cutout")
    public ResponseEntity<CutoutImageGenerateResponse> generateCutoutImage(@AuthenticationPrincipal JwtUserPrincipal principal, @RequestBody @Valid CutoutImageGenerateRequest request) {

        return ResponseEntity.ok(imageService.generateCutoutImage(principal.userId(), request));
    }

    @PostMapping("/v1/image")
    public ResponseEntity<ImageGenerateResponse> generateImage(@AuthenticationPrincipal JwtUserPrincipal principal, @RequestBody @Valid ImageGenerateRequest request) {

        return ResponseEntity.ok(imageService.generateImage(principal.userId(), request));
    }

    @GetMapping("/v1/image/{imageId}")
    public ResponseEntity<ImageDetailResponse> getImage(@PathVariable Long imageId) {

        return ResponseEntity.ok(imageService.getImage(imageId));
    }



}
