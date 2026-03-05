package net.studioxai.studioxBe.domain.chat.dto.response;

import net.studioxai.studioxBe.domain.chat.entity.ChatMessage;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageRole;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageType;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageResponse(
        Long messageId,
        MessageRole role,
        MessageType messageType,
        String content,
        @ImageUrl List<String> imageKeys,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        List<String> keys = null;
        if (message.getImageKeys() != null && !message.getImageKeys().isBlank()) {
            keys = List.of(message.getImageKeys().split(","));
        }
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getMessageType(),
                message.getContent(),
                keys,
                message.getCreatedAt()
        );
    }
}
