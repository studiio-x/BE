package net.studioxai.studioxBe.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageGenerateRequest(
        @NotBlank String cutoutImageObjectKey,
        @NotNull Long templateId,
        @NotNull Long projectId
) {}