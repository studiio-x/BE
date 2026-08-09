package net.studioxai.studioxBe.domain.image.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.VideoGenerateRequestDto;
import net.studioxai.studioxBe.domain.image.dto.response.VideoGenerateResponseDto;
import net.studioxai.studioxBe.domain.image.entity.enums.MotionType;
import net.studioxai.studioxBe.domain.image.entity.enums.QualityType;
import net.studioxai.studioxBe.domain.image.service.VideoService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import net.studioxai.studioxBe.infra.s3.S3Url;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("")
    public ResponseEntity<VideoGenerateResponseDto> generateVideo(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam MotionType motionType,
            @RequestParam QualityType qualityType,
            @RequestBody VideoGenerateRequestDto requestDto) {

        VideoGenerateResponseDto response = videoService.generateVideo(principal.userId(), requestDto, motionType, qualityType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/images")
    public S3Url uploadVideoImage() {
        return videoService.getVideoImageUrl();
    }
}