package net.studioxai.studioxBe.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatStatus;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatStatus status;

    @Column(name = "pending_concept_keys", length = 2000)
    private String pendingConceptKeys;

    @Column(name = "pending_prompt", length = 2000)
    private String pendingPrompt;

    public static ChatRoom create(Project project) {
        return ChatRoom.builder()
                .project(project)
                .status(ChatStatus.IDLE)
                .build();
    }

    public void startConceptSelection(String conceptKeys, String prompt) {
        this.status = ChatStatus.AWAITING_CONCEPT_SELECT;
        this.pendingConceptKeys = conceptKeys;
        this.pendingPrompt = prompt;
    }

    public void completeConceptSelection() {
        this.status = ChatStatus.IDLE;
        this.pendingConceptKeys = null;
        this.pendingPrompt = null;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRoom(Project project, ChatStatus status) {
        this.project = project;
        this.status = status;
    }
}
