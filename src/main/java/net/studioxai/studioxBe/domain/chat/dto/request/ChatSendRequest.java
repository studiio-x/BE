package net.studioxai.studioxBe.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatSendRequest(
        @NotBlank String content,
        String referenceImageObjectKey,
        String maskImageObjectKey,
        Long imageId
) {
}
