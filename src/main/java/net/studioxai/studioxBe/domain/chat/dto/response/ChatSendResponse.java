package net.studioxai.studioxBe.domain.chat.dto.response;

import net.studioxai.studioxBe.domain.chat.entity.enums.ChatMode;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

import java.util.List;

public record ChatSendResponse(
        ChatMode mode,
        Long messageId,
        String aiText,
        @ImageUrl List<String> imageKeys
) {
    public static ChatSendResponse concept(Long messageId, String aiText, List<String> imageKeys) {
        return new ChatSendResponse(ChatMode.CONCEPT, messageId, aiText, imageKeys);
    }

    public static ChatSendResponse refine(Long messageId, String aiText, String imageKey) {
        return new ChatSendResponse(ChatMode.REFINE, messageId, aiText, List.of(imageKey));
    }
}
