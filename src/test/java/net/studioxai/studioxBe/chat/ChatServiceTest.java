package net.studioxai.studioxBe.chat;

import net.studioxai.studioxBe.domain.chat.dto.request.ChatSendRequest;
import net.studioxai.studioxBe.domain.chat.dto.request.ConceptSelectRequest;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatHistoryResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatMessageResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatSendResponse;
import net.studioxai.studioxBe.domain.chat.entity.ChatMessage;
import net.studioxai.studioxBe.domain.chat.entity.ChatRoom;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatMode;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatStatus;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageRole;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageType;
import net.studioxai.studioxBe.domain.chat.exception.ChatExceptionHandler;
import net.studioxai.studioxBe.domain.chat.repository.ChatMessageRepository;
import net.studioxai.studioxBe.domain.chat.repository.ChatRoomRepository;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.service.ProjectService;
import net.studioxai.studioxBe.domain.chat.service.ChatService;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiChatClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private ProjectService projectService;
    @Mock private FolderManagerService folderManagerService;
    @Mock private GeminiChatClient geminiChatClient;
    @Mock private S3ImageUploader s3ImageUploader;
    @Mock private S3ImageLoader s3ImageLoader;
    @Mock private S3UrlHandler s3UrlHandler;

    @InjectMocks
    private ChatService chatService;

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long FOLDER_ID = 10L;

    private Project project;
    private Folder folder;
    private ChatRoom chatRoom;
    private String base64Data;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatService, "retentionDays", 30);

        folder = mock(Folder.class);
        lenient().when(folder.getId()).thenReturn(FOLDER_ID);

        project = mock(Project.class);
        lenient().when(project.getId()).thenReturn(PROJECT_ID);
        lenient().when(project.getFolder()).thenReturn(folder);

        chatRoom = ChatRoom.create(project);
        ReflectionTestUtils.setField(chatRoom, "id", 1L);

        base64Data = Base64.getEncoder().encodeToString("test-image".getBytes());
    }

    private void stubProjectAccess() {
        when(projectService.getProjectWithFolderById(PROJECT_ID)).thenReturn(project);
        doNothing().when(folderManagerService).isUserWritable(USER_ID, FOLDER_ID);
    }

    private void stubChatRoomExists() {
        when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(chatRoom));
    }

    // =====================================================
    // getChatHistory
    // =====================================================
    @Nested
    @DisplayName("getChatHistory")
    class GetChatHistory {

        @Test
        @DisplayName("빈 채팅방 히스토리 조회 성공")
        void getChatHistory_empty_success() {
            stubProjectAccess();
            stubChatRoomExists();
            when(chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtDesc(
                    eq(chatRoom), any(), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());

            ChatHistoryResponse response = chatService.getChatHistory(USER_ID, PROJECT_ID, 0);

            assertThat(response.chatRoomId()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo(ChatStatus.IDLE);
            assertThat(response.messages()).isEmpty();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("채팅방이 없으면 자동 생성")
        void getChatHistory_createsChatRoomIfNotExists() {
            stubProjectAccess();
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.empty());
            when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
            when(chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtDesc(
                    eq(chatRoom), any(), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());

            ChatHistoryResponse response = chatService.getChatHistory(USER_ID, PROJECT_ID, 0);

            verify(chatRoomRepository).save(any(ChatRoom.class));
            assertThat(response.status()).isEqualTo(ChatStatus.IDLE);
        }

        @Test
        @DisplayName("메시지가 있는 히스토리 조회")
        void getChatHistory_withMessages() {
            stubProjectAccess();
            stubChatRoomExists();

            ChatMessage userMsg = ChatMessage.createUserText(chatRoom, "테스트 메시지");
            ReflectionTestUtils.setField(userMsg, "id", 1L);
            ChatMessage aiMsg = ChatMessage.createConceptImages(chatRoom, "AI 응답", "key1,key2,key3,key4");
            ReflectionTestUtils.setField(aiMsg, "id", 2L);

            when(chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtDesc(
                    eq(chatRoom), any(), any(Pageable.class)))
                    .thenReturn(List.of(aiMsg, userMsg));

            ChatHistoryResponse response = chatService.getChatHistory(USER_ID, PROJECT_ID, 0);

            assertThat(response.messages()).hasSize(2);
            // reversed: userMsg first, aiMsg second
            assertThat(response.messages().get(0).role()).isEqualTo(MessageRole.USER);
            assertThat(response.messages().get(1).role()).isEqualTo(MessageRole.ASSISTANT);
        }

        @Test
        @DisplayName("PAGE_SIZE(50)개 결과 시 hasNext=true")
        void getChatHistory_hasNext() {
            stubProjectAccess();
            stubChatRoomExists();

            List<ChatMessage> fiftyMessages = Collections.nCopies(50,
                    ChatMessage.createUserText(chatRoom, "msg"));

            when(chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtDesc(
                    eq(chatRoom), any(), any(Pageable.class)))
                    .thenReturn(fiftyMessages);

            ChatHistoryResponse response = chatService.getChatHistory(USER_ID, PROJECT_ID, 0);

            assertThat(response.hasNext()).isTrue();
        }
    }

    // =====================================================
    // sendMessage
    // =====================================================
    @Nested
    @DisplayName("sendMessage")
    class SendMessage {

        private Image currentImage;

        @BeforeEach
        void setUpImage() {
            currentImage = mock(Image.class);
            lenient().when(currentImage.getImageObjectKey()).thenReturn("images/100/cutout/test.png");
            lenient().when(currentImage.getProject()).thenReturn(project);
        }

        private void stubSendMessageDependencies() {
            stubProjectAccess();
            stubChatRoomExists();

            when(imageRepository.findTopByProjectOrderByCreatedAtDesc(project))
                    .thenReturn(Optional.of(currentImage));
            when(s3ImageLoader.loadAsBase64(anyString())).thenReturn(base64Data);
            when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(eq(chatRoom), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());
            when(geminiChatClient.generateConceptImages(anyString(), anyList(), anyString(), any(), any()))
                    .thenReturn(List.of(base64Data, base64Data, base64Data, base64Data));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 1L);
                        return msg;
                    });
        }

        @Test
        @DisplayName("CONCEPT 모드 - 텍스트만 전송 성공")
        void sendMessage_concept_textOnly_success() {
            stubSendMessageDependencies();
            ChatSendRequest request = new ChatSendRequest("배경을 바다로 변경해주세요", null, null, null);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request);

            assertThat(response.mode()).isEqualTo(ChatMode.CONCEPT);
            assertThat(response.messageId()).isEqualTo(1L);
            assertThat(response.imageKeys()).hasSize(4);
            assertThat(response.aiText()).contains("4개의 컨셉 이미지");
            assertThat(chatRoom.getStatus()).isEqualTo(ChatStatus.AWAITING_CONCEPT_SELECT);

            verify(s3ImageUploader, times(4)).upload(anyString(), any(byte[].class));
            verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("CONCEPT 모드 - 참조 이미지 포함 전송 성공")
        void sendMessage_concept_withReference_success() {
            stubSendMessageDependencies();
            ChatSendRequest request = new ChatSendRequest(
                    "이 스타일로 변경해주세요", "images/100/chat/reference/ref.png", null, null);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request);

            assertThat(response.imageKeys()).hasSize(4);
            verify(s3ImageLoader, atLeast(2)).loadAsBase64(anyString());
        }

        @Test
        @DisplayName("CONCEPT 모드 - 마스크 이미지 포함 전송 성공")
        void sendMessage_concept_withMask_success() {
            stubSendMessageDependencies();
            ChatSendRequest request = new ChatSendRequest(
                    "마스크 영역만 변경", null, "images/100/chat/mask/mask.png", null);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request);

            assertThat(response.imageKeys()).hasSize(4);
        }

        @Test
        @DisplayName("CONCEPT 모드 - 특정 imageId 지정 전송 성공")
        void sendMessage_concept_withImageId_success() {
            stubProjectAccess();
            stubChatRoomExists();

            ReflectionTestUtils.setField(currentImage, "id", 5L);
            when(imageRepository.findById(5L)).thenReturn(Optional.of(currentImage));
            when(s3ImageLoader.loadAsBase64(anyString())).thenReturn(base64Data);
            when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(eq(chatRoom), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());
            when(geminiChatClient.generateConceptImages(anyString(), anyList(), anyString(), any(), any()))
                    .thenReturn(List.of(base64Data, base64Data, base64Data, base64Data));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 1L);
                        return msg;
                    });

            ChatSendRequest request = new ChatSendRequest("색감 변경", null, null, 5L);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request);

            assertThat(response.imageKeys()).hasSize(4);
            verify(imageRepository).findById(5L);
            verify(imageRepository, never()).findTopByProjectOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("REFINE 모드 - 텍스트만 전송 성공")
        void sendMessage_refine_textOnly_success() {
            stubProjectAccess();
            stubChatRoomExists();

            when(imageRepository.findTopByProjectOrderByCreatedAtDesc(project))
                    .thenReturn(Optional.of(currentImage));
            when(s3ImageLoader.loadAsBase64(anyString())).thenReturn(base64Data);
            when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(eq(chatRoom), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());
            when(geminiChatClient.generateRefineImage(anyString(), anyString(), any(), any()))
                    .thenReturn(base64Data);
            when(imageRepository.save(any(Image.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 1L);
                        return msg;
                    });

            ChatSendRequest request = new ChatSendRequest("색상만 살짝 바꿔줘", null, null, null);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.REFINE, request);

            assertThat(response.mode()).isEqualTo(ChatMode.REFINE);
            assertThat(response.messageId()).isEqualTo(1L);
            assertThat(response.imageKeys()).hasSize(1);
            assertThat(response.aiText()).contains("수정");
            assertThat(chatRoom.getStatus()).isEqualTo(ChatStatus.IDLE);

            verify(s3ImageUploader).upload(contains("images/100/chat/refine/"), any(byte[].class));
            verify(imageRepository).save(any(Image.class));
            verify(project).updateThumbnailObjectKey(anyString());
        }

        @Test
        @DisplayName("REFINE 모드 - reference/mask 포함 전송 성공")
        void sendMessage_refine_withReferenceAndMask_success() {
            stubProjectAccess();
            stubChatRoomExists();

            when(imageRepository.findTopByProjectOrderByCreatedAtDesc(project))
                    .thenReturn(Optional.of(currentImage));
            when(s3ImageLoader.loadAsBase64(anyString())).thenReturn(base64Data);
            when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(eq(chatRoom), any(Pageable.class)))
                    .thenReturn(Collections.emptyList());
            when(geminiChatClient.generateRefineImage(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(base64Data);
            when(imageRepository.save(any(Image.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 1L);
                        return msg;
                    });

            ChatSendRequest request = new ChatSendRequest(
                    "이 스타일로 변경", "images/100/chat/reference/ref.png", "images/100/chat/mask/mask.png", null);

            ChatSendResponse response = chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.REFINE, request);

            assertThat(response.mode()).isEqualTo(ChatMode.REFINE);
            assertThat(response.imageKeys()).hasSize(1);
            verify(s3ImageLoader, atLeast(3)).loadAsBase64(anyString());
        }

        @Test
        @DisplayName("AWAITING 상태에서 메시지 전송 시 409 에러")
        void sendMessage_whileAwaiting_throws409() {
            stubProjectAccess();
            stubChatRoomExists();
            chatRoom.startConceptSelection("key1,key2,key3,key4", "prompt");

            ChatSendRequest request = new ChatSendRequest("또 다른 요청", null, null, null);

            assertThatThrownBy(() -> chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("REFINE 모드도 AWAITING 상태에서 전송 시 409 에러")
        void sendMessage_refine_whileAwaiting_throws409() {
            stubProjectAccess();
            stubChatRoomExists();
            chatRoom.startConceptSelection("key1,key2,key3,key4", "prompt");

            ChatSendRequest request = new ChatSendRequest("색상 변경", null, null, null);

            assertThatThrownBy(() -> chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.REFINE, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("프로젝트에 이미지가 없으면 404 에러")
        void sendMessage_noImage_throws404() {
            stubProjectAccess();
            stubChatRoomExists();
            when(imageRepository.findTopByProjectOrderByCreatedAtDesc(project))
                    .thenReturn(Optional.empty());

            ChatSendRequest request = new ChatSendRequest("테스트", null, null, null);

            assertThatThrownBy(() -> chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("다른 프로젝트의 이미지 지정 시 400 에러")
        void sendMessage_imageNotInProject_throws400() {
            stubProjectAccess();
            stubChatRoomExists();

            Project otherProject = mock(Project.class);
            when(otherProject.getId()).thenReturn(999L);

            Image otherImage = mock(Image.class);
            when(otherImage.getProject()).thenReturn(otherProject);

            when(imageRepository.findById(5L)).thenReturn(Optional.of(otherImage));

            ChatSendRequest request = new ChatSendRequest("테스트", null, null, 5L);

            assertThatThrownBy(() -> chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("존재하지 않는 imageId 지정 시 404 에러")
        void sendMessage_imageNotFound_throws404() {
            stubProjectAccess();
            stubChatRoomExists();
            when(imageRepository.findById(999L)).thenReturn(Optional.empty());

            ChatSendRequest request = new ChatSendRequest("테스트", null, null, 999L);

            assertThatThrownBy(() -> chatService.sendMessage(USER_ID, PROJECT_ID, ChatMode.CONCEPT, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }
    }

    // =====================================================
    // selectConcept
    // =====================================================
    @Nested
    @DisplayName("selectConcept")
    class SelectConcept {

        @BeforeEach
        void setUpAwaitingState() {
            chatRoom.startConceptSelection("key0,key1,key2,key3", "배경을 바다로");
        }

        @Test
        @DisplayName("컨셉 선택 성공 - 최종 이미지 생성")
        void selectConcept_success() {
            stubProjectAccess();
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(chatRoom));
            when(s3ImageLoader.loadAsBase64("key0")).thenReturn(base64Data);
            when(geminiChatClient.generateFinalImage(anyString(), anyString())).thenReturn(base64Data);
            when(imageRepository.save(any(Image.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 3L);
                        return msg;
                    });

            ConceptSelectRequest request = new ConceptSelectRequest(0);

            ChatMessageResponse response = chatService.selectConcept(USER_ID, PROJECT_ID, request);

            assertThat(response.messageId()).isEqualTo(3L);
            assertThat(response.role()).isEqualTo(MessageRole.ASSISTANT);
            assertThat(response.messageType()).isEqualTo(MessageType.FINAL_IMAGE);
            assertThat(response.content()).contains("최종 이미지");
            assertThat(chatRoom.getStatus()).isEqualTo(ChatStatus.IDLE);
            assertThat(chatRoom.getPendingConceptKeys()).isNull();

            verify(s3ImageUploader).upload(contains("images/100/chat/final/"), any(byte[].class));
            verify(imageRepository).save(any(Image.class));
            verify(project).updateThumbnailObjectKey(anyString());
        }

        @Test
        @DisplayName("다른 인덱스(3) 선택 성공")
        void selectConcept_index3_success() {
            stubProjectAccess();
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(chatRoom));
            when(s3ImageLoader.loadAsBase64("key3")).thenReturn(base64Data);
            when(geminiChatClient.generateFinalImage(anyString(), anyString())).thenReturn(base64Data);
            when(imageRepository.save(any(Image.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(chatMessageRepository.save(any(ChatMessage.class)))
                    .thenAnswer(inv -> {
                        ChatMessage msg = inv.getArgument(0);
                        ReflectionTestUtils.setField(msg, "id", 3L);
                        return msg;
                    });

            ConceptSelectRequest request = new ConceptSelectRequest(3);

            ChatMessageResponse response = chatService.selectConcept(USER_ID, PROJECT_ID, request);

            assertThat(response.messageType()).isEqualTo(MessageType.FINAL_IMAGE);
            verify(s3ImageLoader).loadAsBase64("key3");
        }

        @Test
        @DisplayName("IDLE 상태에서 선택 시 409 에러")
        void selectConcept_notAwaiting_throws409() {
            stubProjectAccess();
            ChatRoom idleChatRoom = ChatRoom.create(project);
            ReflectionTestUtils.setField(idleChatRoom, "id", 2L);
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(idleChatRoom));

            ConceptSelectRequest request = new ConceptSelectRequest(0);

            assertThatThrownBy(() -> chatService.selectConcept(USER_ID, PROJECT_ID, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("유효하지 않은 인덱스 선택 시 에러")
        void selectConcept_invalidIndex_throwsError() {
            stubProjectAccess();
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(chatRoom));

            ConceptSelectRequest request = new ConceptSelectRequest(4);

            assertThatThrownBy(() -> chatService.selectConcept(USER_ID, PROJECT_ID, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }

        @Test
        @DisplayName("채팅방 없으면 404 에러")
        void selectConcept_chatRoomNotFound_throws404() {
            stubProjectAccess();
            when(chatRoomRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.empty());

            ConceptSelectRequest request = new ConceptSelectRequest(0);

            assertThatThrownBy(() -> chatService.selectConcept(USER_ID, PROJECT_ID, request))
                    .isInstanceOf(ChatExceptionHandler.class);
        }
    }

    // =====================================================
    // issueReferencePresign / issueMaskPresign
    // =====================================================
    @Nested
    @DisplayName("issuePresign")
    class IssuePresign {

        @Test
        @DisplayName("Reference presign 발급 성공")
        void issueReferencePresign_success() {
            stubProjectAccess();
            S3Url s3Url = S3Url.to("https://s3.amazonaws.com/upload", "images/100/chat/reference/uuid");
            when(s3UrlHandler.handle(contains("chat/reference"))).thenReturn(s3Url);

            S3Url response = chatService.issueReferencePresign(USER_ID, PROJECT_ID);

            assertThat(response.getUploadUrl()).isEqualTo("https://s3.amazonaws.com/upload");
            assertThat(response.getObjectKey()).contains("chat/reference");
        }

        @Test
        @DisplayName("Mask presign 발급 성공")
        void issueMaskPresign_success() {
            stubProjectAccess();
            S3Url s3Url = S3Url.to("https://s3.amazonaws.com/upload", "images/100/chat/mask/uuid");
            when(s3UrlHandler.handle(contains("chat/mask"))).thenReturn(s3Url);

            S3Url response = chatService.issueMaskPresign(USER_ID, PROJECT_ID);

            assertThat(response.getUploadUrl()).isEqualTo("https://s3.amazonaws.com/upload");
            assertThat(response.getObjectKey()).contains("chat/mask");
        }
    }
}
