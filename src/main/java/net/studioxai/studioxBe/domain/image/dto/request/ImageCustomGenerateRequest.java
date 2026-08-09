package net.studioxai.studioxBe.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageCustomGenerateRequest(
        @NotBlank String cutoutImageObjectKey,
        @NotNull String customBackgroundImageObjectKey,
        @NotNull Long projectId
) {
}
