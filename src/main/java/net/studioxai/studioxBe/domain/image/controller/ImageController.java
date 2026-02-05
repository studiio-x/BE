package net.studioxai.studioxBe.domain.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
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
    public ResponseEntity<RawPresignResponse> issueRawPresign(@AuthenticationPrincipal JwtUserPrincipal principal) {

        return ResponseEntity.ok(imageService.issueRawPresign(principal.userId()));
    }


    @PostMapping("/v1/image/cutout")
    public ResponseEntity<CutoutResponse> cutout(@AuthenticationPrincipal JwtUserPrincipal principal, @RequestBody @Valid CutoutRequest request) {

        return ResponseEntity.ok(imageService.cutout(principal.userId(), request));
    }

    @PostMapping("/v1/image/generate")
    public ResponseEntity<ImageGenerateResponse> generate(@AuthenticationPrincipal JwtUserPrincipal principal, @RequestBody @Valid ImageGenerateRequest request) {

        return ResponseEntity.ok(imageService.generateResultImage(principal.userId(), request));
    }

    @GetMapping("/v1/image/cutout/{cutoutImageId}")
    public ResponseEntity<CutoutImageResponse> getCutoutImage(@PathVariable Long cutoutImageId) {

        return ResponseEntity.ok(imageService.getCutoutImage(cutoutImageId));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImage(@PathVariable Long imageId) {

        return ResponseEntity.ok(imageService.getImage(imageId));
    }



}
