package net.studioxai.studioxBe.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.chat.dto.request.ChatSendRequest;
import net.studioxai.studioxBe.domain.chat.dto.request.ConceptSelectRequest;
import net.studioxai.studioxBe.domain.chat.dto.response.*;
import net.studioxai.studioxBe.domain.chat.entity.ChatMessage;
import net.studioxai.studioxBe.domain.chat.entity.ChatRoom;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatStatus;
import net.studioxai.studioxBe.domain.chat.exception.ChatErrorCode;
import net.studioxai.studioxBe.domain.chat.exception.ChatExceptionHandler;
import net.studioxai.studioxBe.domain.chat.repository.ChatMessageRepository;
import net.studioxai.studioxBe.domain.chat.repository.ChatRoomRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.service.ProjectService;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiChatClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageRepository imageRepository;

    private final ProjectService projectService;
    private final FolderManagerService folderManagerService;

    private final GeminiChatClient geminiChatClient;
    private final S3ImageUploader s3ImageUploader;
    private final S3ImageLoader s3ImageLoader;
    private final S3UrlHandler s3UrlHandler;

    private static final int CONTEXT_WINDOW_SIZE = 10;
    private static final int PAGE_SIZE = 50;

    @Value("${chat.retention-days:30}")
    private int retentionDays;

    @Transactional
    public ChatHistoryResponse getChatHistory(Long userId, Long projectId, int page) {
        Project project = getProjectAndVerifyAccess(userId, projectId);

        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(project)));

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        List<ChatMessage> recent = chatMessageRepository.findByChatRoomAndCreatedAtAfterOrderByCreatedAtDesc(
                chatRoom, cutoff, PageRequest.of(page, PAGE_SIZE));

        List<ChatMessage> reversed = new ArrayList<>(recent);
        Collections.reverse(reversed);

        List<ChatMessageResponse> messages = reversed.stream()
                .map(ChatMessageResponse::from)
                .toList();

        boolean hasNext = recent.size() == PAGE_SIZE;

        return ChatHistoryResponse.of(chatRoom.getId(), chatRoom.getStatus(), messages, hasNext);
    }

    @Transactional
    public ConceptImagesResponse sendMessage(Long userId, Long projectId, ChatSendRequest request) {
        Project project = getProjectAndVerifyAccess(userId, projectId);

        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(project)));

        if (chatRoom.getStatus() == ChatStatus.AWAITING_CONCEPT_SELECT) {
            throw new ChatExceptionHandler(ChatErrorCode.CONCEPT_SELECTION_PENDING);
        }

        // 1. Save user message
        ChatMessage userMessage;
        List<String> attachedKeys = new ArrayList<>();
        if (request.referenceImageObjectKey() != null && !request.referenceImageObjectKey().isBlank()) {
            attachedKeys.add(request.referenceImageObjectKey());
        }
        if (request.maskImageObjectKey() != null && !request.maskImageObjectKey().isBlank()) {
            attachedKeys.add(request.maskImageObjectKey());
        }
        if (!attachedKeys.isEmpty()) {
            userMessage = ChatMessage.createUserImageAttachment(
                    chatRoom, request.content(), String.join(",", attachedKeys));
        } else {
            userMessage = ChatMessage.createUserText(chatRoom, request.content());
        }
        chatMessageRepository.save(userMessage);

        // 2. Build context for AI
        List<ChatMessage> contextMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(
                chatRoom, PageRequest.of(0, CONTEXT_WINDOW_SIZE));
        contextMessages = new ArrayList<>(contextMessages);
        Collections.reverse(contextMessages);

        // 3. Load current image (user-specified or latest)
        Image currentImage;
        if (request.imageId() != null) {
            currentImage = imageRepository.findById(request.imageId())
                    .orElseThrow(() -> new ChatExceptionHandler(ChatErrorCode.IMAGE_NOT_FOUND));
            if (!currentImage.getProject().getId().equals(project.getId())) {
                throw new ChatExceptionHandler(ChatErrorCode.IMAGE_NOT_IN_PROJECT);
            }
        } else {
            currentImage = imageRepository.findTopByProjectOrderByCreatedAtDesc(project)
                    .orElseThrow(() -> new ChatExceptionHandler(ChatErrorCode.IMAGE_NOT_FOUND));
        }
        String currentImageBase64 = s3ImageLoader.loadAsBase64(currentImage.getImageObjectKey());

        // 4. Load reference image if provided
        String referenceBase64 = null;
        if (request.referenceImageObjectKey() != null && !request.referenceImageObjectKey().isBlank()) {
            referenceBase64 = s3ImageLoader.loadAsBase64(request.referenceImageObjectKey());
        }

        // 5. Load mask image if provided
        String maskBase64 = null;
        if (request.maskImageObjectKey() != null && !request.maskImageObjectKey().isBlank()) {
            maskBase64 = s3ImageLoader.loadAsBase64(request.maskImageObjectKey());
        }

        // 6. Generate 4 concept images via Gemini (parallel)
        List<String> conceptBase64List = geminiChatClient.generateConceptImages(
                request.content(), contextMessages, currentImageBase64, referenceBase64, maskBase64);

        // 7. Upload 4 concept images to S3
        List<String> conceptKeys = new ArrayList<>();
        for (String conceptBase64 : conceptBase64List) {
            String key = "images/" + project.getId() + "/chat/concept/" + UUID.randomUUID() + ".png";
            byte[] bytes = Base64.getDecoder().decode(conceptBase64);
            s3ImageUploader.upload(key, bytes);
            conceptKeys.add(key);
        }

        String conceptKeysJoined = String.join(",", conceptKeys);

        // 8. Save AI response message
        ChatMessage aiMessage = ChatMessage.createConceptImages(
                chatRoom,
                "요청하신 내용을 기반으로 4개의 컨셉 이미지를 생성했습니다. 마음에 드는 컨셉을 선택해주세요.",
                conceptKeysJoined);
        chatMessageRepository.save(aiMessage);

        // 9. Update chat room state
        chatRoom.startConceptSelection(conceptKeysJoined, request.content());

        return ConceptImagesResponse.of(aiMessage.getId(), aiMessage.getContent(), conceptKeys);
    }

    @Transactional
    public ChatMessageResponse selectConcept(Long userId, Long projectId, ConceptSelectRequest request) {
        Project project = getProjectAndVerifyAccess(userId, projectId);

        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ChatExceptionHandler(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        if (chatRoom.getStatus() != ChatStatus.AWAITING_CONCEPT_SELECT) {
            throw new ChatExceptionHandler(ChatErrorCode.NO_PENDING_CONCEPT);
        }

        // 1. Get the selected concept image
        String[] conceptKeys = chatRoom.getPendingConceptKeys().split(",");
        if (request.selectedIndex() >= conceptKeys.length) {
            throw new ChatExceptionHandler(ChatErrorCode.INVALID_CONCEPT_INDEX);
        }
        String selectedConceptKey = conceptKeys[request.selectedIndex()];
        String selectedConceptBase64 = s3ImageLoader.loadAsBase64(selectedConceptKey);

        // 2. Generate final image via Gemini
        String finalBase64 = geminiChatClient.generateFinalImage(
                chatRoom.getPendingPrompt(), selectedConceptBase64);

        // 4. Upload final image to S3
        String finalImageKey = "images/" + project.getId() + "/chat/final/" + UUID.randomUUID() + ".png";
        byte[] finalBytes = Base64.getDecoder().decode(finalBase64);
        s3ImageUploader.upload(finalImageKey, finalBytes);

        // 5. Save as Image entity (appears in History tab)
        Image image = Image.create(project, finalImageKey);
        imageRepository.save(image);

        // 6. Update project thumbnail
        project.updatethumbnailObjectKey(finalImageKey);

        // 7. Save AI response message
        ChatMessage aiMessage = ChatMessage.createFinalImage(
                chatRoom,
                "선택하신 컨셉을 기반으로 최종 이미지를 생성했습니다.",
                finalImageKey);
        chatMessageRepository.save(aiMessage);

        // 8. Reset chat room state
        chatRoom.completeConceptSelection();

        return ChatMessageResponse.from(aiMessage);
    }

    public ChatSendPresignResponse issueReferencePresign(Long userId, Long projectId) {
        getProjectAndVerifyAccess(userId, projectId);
        S3Url s3Url = s3UrlHandler.handle("images/" + projectId + "/chat/reference");
        return ChatSendPresignResponse.of(s3Url.getUploadUrl(), s3Url.getObjectKey());
    }

    public ChatSendPresignResponse issueMaskPresign(Long userId, Long projectId) {
        getProjectAndVerifyAccess(userId, projectId);
        S3Url s3Url = s3UrlHandler.handle("images/" + projectId + "/chat/mask");
        return ChatSendPresignResponse.of(s3Url.getUploadUrl(), s3Url.getObjectKey());
    }

    private Project getProjectAndVerifyAccess(Long userId, Long projectId) {
        Project project = projectService.getProjectWithFolderById(projectId);
        folderManagerService.isUserWritable(userId, project.getFolder().getId());
        return project;
    }

}
