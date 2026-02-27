package net.studioxai.studioxBe.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageRole;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageType;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_room_created", columnList = "chat_room_id, created_at")
})
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "content", length = 4000)
    private String content;

    @Column(name = "image_keys", length = 2000)
    private String imageKeys;

    public static ChatMessage createUserText(ChatRoom chatRoom, String content) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .role(MessageRole.USER)
                .messageType(MessageType.TEXT)
                .content(content)
                .build();
    }

    public static ChatMessage createUserImageAttachment(
            ChatRoom chatRoom, String caption, String imageObjectKey) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .role(MessageRole.USER)
                .messageType(MessageType.IMAGE_ATTACHMENT)
                .content(caption)
                .imageKeys(imageObjectKey)
                .build();
    }

    public static ChatMessage createConceptImages(
            ChatRoom chatRoom, String textResponse, String conceptImageKeys) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .role(MessageRole.ASSISTANT)
                .messageType(MessageType.CONCEPT_IMAGES)
                .content(textResponse)
                .imageKeys(conceptImageKeys)
                .build();
    }

    public static ChatMessage createFinalImage(
            ChatRoom chatRoom, String textResponse, String finalImageKey) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .role(MessageRole.ASSISTANT)
                .messageType(MessageType.FINAL_IMAGE)
                .content(textResponse)
                .imageKeys(finalImageKey)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessage(
            ChatRoom chatRoom,
            MessageRole role,
            MessageType messageType,
            String content,
            String imageKeys
    ) {
        this.chatRoom = chatRoom;
        this.role = role;
        this.messageType = messageType;
        this.content = content;
        this.imageKeys = imageKeys;
    }
}
