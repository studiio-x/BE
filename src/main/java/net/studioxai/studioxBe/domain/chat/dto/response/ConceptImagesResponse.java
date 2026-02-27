package net.studioxai.studioxBe.domain.chat.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

import java.util.List;

public record ConceptImagesResponse(
        Long messageId,
        String aiText,
        @ImageUrl List<String> conceptImageKeys
) {
    public static ConceptImagesResponse of(Long messageId, String aiText, List<String> conceptImageKeys) {
        return new ConceptImagesResponse(messageId, aiText, conceptImageKeys);
    }
}
