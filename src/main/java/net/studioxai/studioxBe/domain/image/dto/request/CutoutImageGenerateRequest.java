package net.studioxai.studioxBe.domain.image.dto.request;
import jakarta.validation.constraints.NotBlank;

public record CutoutImageGenerateRequest(
        @NotBlank String rawObjectKey
) {}
