package net.studioxai.studioxBe.domain.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.ImageGenerateResponse;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/v1/image/generate")
    public ResponseEntity<ImageGenerateResponse> generateImage(@AuthenticationPrincipal JwtUserPrincipal principal, @Valid @RequestBody ImageGenerateRequest request) {
        Image image = imageService.generateAdImage(
                principal.userId(),
                request.folderId(),
                request.templateId(),
                request.rawImageUrl()
        );

        return ResponseEntity.ok(
                new ImageGenerateResponse(
                        image.getId(),
                        image.getImageUrl()
                )
        );
    }


}
