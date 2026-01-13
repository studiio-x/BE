package net.studioxai.studioxBe.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageGenerateRequest(
        @NotNull Long folderId,
        @NotNull Long templateId,
        @NotBlank String rawImageUrl
) {
}