package net.studioxai.studioxBe.domain.chat.dto.response;

import net.studioxai.studioxBe.domain.chat.entity.enums.ChatStatus;

import java.util.List;

public record ChatHistoryResponse(
        Long chatRoomId,
        ChatStatus status,
        List<ChatMessageResponse> messages,
        boolean hasNext
) {
    public static ChatHistoryResponse of(Long chatRoomId, ChatStatus status, List<ChatMessageResponse> messages, boolean hasNext) {
        return new ChatHistoryResponse(chatRoomId, status, messages, hasNext);
    }
}
