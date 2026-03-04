package net.studioxai.studioxBe.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.chat.dto.request.ChatSendRequest;
import net.studioxai.studioxBe.domain.chat.dto.request.ConceptSelectRequest;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatHistoryResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatMessageResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatSendResponse;
import net.studioxai.studioxBe.domain.chat.entity.ChatMessage;
import net.studioxai.studioxBe.domain.chat.entity.ChatRoom;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatMode;
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
    public ChatSendResponse sendMessage(Long userId, Long projectId, ChatMode mode, ChatSendRequest request) {
        Project project = getProjectAndVerifyAccess(userId, projectId);

        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(project)));

        if (chatRoom.getStatus() == ChatStatus.AWAITING_CONCEPT_SELECT) {
            throw new ChatExceptionHandler(ChatErrorCode.CONCEPT_SELECTION_PENDING);
        }

        validateImageObjectKeys(projectId, request);

        ChatMessage userMessage = saveUserMessage(chatRoom, request);
        List<ChatMessage> contextMessages = buildAiContext(chatRoom);
        String currentImageBase64 = loadCurrentImageBase64(project, request);
        String referenceBase64 = loadOptionalImageBase64(request.referenceImageObjectKey());
        String maskBase64 = loadOptionalImageBase64(request.maskImageObjectKey());

        if (mode == ChatMode.REFINE) {
            return handleRefineMode(project, chatRoom, request.content(),
                    currentImageBase64, referenceBase64, maskBase64);
        }
        return handleConceptMode(project, chatRoom, request.content(), contextMessages,
                currentImageBase64, referenceBase64, maskBase64);
    }

    private ChatSendResponse handleConceptMode(Project project, ChatRoom chatRoom, String prompt,
                                                List<ChatMessage> contextMessages,
                                                String currentImageBase64,
                                                String referenceBase64, String maskBase64) {
        List<String> conceptKeys = generateAndUploadConcepts(
                project, prompt, contextMessages, currentImageBase64, referenceBase64, maskBase64);

        String conceptKeysJoined = String.join(",", conceptKeys);

        ChatMessage aiMessage = ChatMessage.createConceptImages(
                chatRoom,
                "요청하신 내용을 기반으로 4개의 컨셉 이미지를 생성했습니다. 마음에 드는 컨셉을 선택해주세요.",
                conceptKeysJoined);
        chatMessageRepository.save(aiMessage);

        chatRoom.startConceptSelection(conceptKeysJoined, prompt);

        return ChatSendResponse.concept(aiMessage.getId(), aiMessage.getContent(), conceptKeys);
    }

    private ChatSendResponse handleRefineMode(Project project, ChatRoom chatRoom, String prompt,
                                               String currentImageBase64,
                                               String referenceBase64, String maskBase64) {
        String refineImageKey = generateAndUploadRefineImage(
                project, prompt, currentImageBase64, referenceBase64, maskBase64);

        saveImageAndUpdateProject(project, refineImageKey);

        ChatMessage aiMessage = ChatMessage.createRefineImage(
                chatRoom,
                "요청하신 내용을 반영하여 이미지를 수정했습니다.",
                refineImageKey);
        chatMessageRepository.save(aiMessage);

        return ChatSendResponse.refine(aiMessage.getId(), aiMessage.getContent(), refineImageKey);
    }

    @Transactional
    public ChatMessageResponse selectConcept(Long userId, Long projectId, ConceptSelectRequest request) {
        Project project = getProjectAndVerifyAccess(userId, projectId);

        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ChatExceptionHandler(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        if (chatRoom.getStatus() != ChatStatus.AWAITING_CONCEPT_SELECT) {
            throw new ChatExceptionHandler(ChatErrorCode.NO_PENDING_CONCEPT);
        }

        String selectedConceptBase64 = loadSelectedConceptBase64(chatRoom, request.selectedIndex());
        String finalImageKey = generateAndUploadFinalImage(project, chatRoom.getPendingPrompt(), selectedConceptBase64);
        saveImageAndUpdateProject(project, finalImageKey);

        ChatMessage aiMessage = ChatMessage.createFinalImage(
                chatRoom,
                "선택하신 컨셉을 기반으로 최종 이미지를 생성했습니다.",
                finalImageKey);
        chatMessageRepository.save(aiMessage);

        chatRoom.completeConceptSelection();

        return ChatMessageResponse.from(aiMessage);
    }

    public S3Url issueReferencePresign(Long userId, Long projectId) {
        getProjectAndVerifyAccess(userId, projectId);
        return s3UrlHandler.handle("images/" + projectId + "/chat/reference");
    }

    public S3Url issueMaskPresign(Long userId, Long projectId) {
        getProjectAndVerifyAccess(userId, projectId);
        return s3UrlHandler.handle("images/" + projectId + "/chat/mask");
    }

    private String loadSelectedConceptBase64(ChatRoom chatRoom, int selectedIndex) {
        String pendingKeys = chatRoom.getPendingConceptKeys();
        if (pendingKeys == null || pendingKeys.isBlank()) {
            throw new ChatExceptionHandler(ChatErrorCode.NO_PENDING_CONCEPT);
        }
        String[] conceptKeys = pendingKeys.split(",");
        if (selectedIndex >= conceptKeys.length) {
            throw new ChatExceptionHandler(ChatErrorCode.INVALID_CONCEPT_INDEX);
        }
        return s3ImageLoader.loadAsBase64(conceptKeys[selectedIndex]);
    }

    private String generateAndUploadRefineImage(Project project, String prompt,
                                                  String currentImageBase64,
                                                  String referenceBase64, String maskBase64) {
        String refineBase64 = geminiChatClient.generateRefineImage(
                prompt, currentImageBase64, referenceBase64, maskBase64);
        String refineImageKey = "images/" + project.getId() + "/chat/refine/" + UUID.randomUUID() + ".png";
        byte[] refineBytes = Base64.getDecoder().decode(refineBase64);
        s3ImageUploader.upload(refineImageKey, refineBytes);
        return refineImageKey;
    }

    private String generateAndUploadFinalImage(Project project, String prompt, String selectedConceptBase64) {
        String finalBase64 = geminiChatClient.generateFinalImage(prompt, selectedConceptBase64);
        String finalImageKey = "images/" + project.getId() + "/chat/final/" + UUID.randomUUID() + ".png";
        byte[] finalBytes = Base64.getDecoder().decode(finalBase64);
        s3ImageUploader.upload(finalImageKey, finalBytes);
        return finalImageKey;
    }

    private void saveImageAndUpdateProject(Project project, String finalImageKey) {
        Image image = Image.create(project, finalImageKey);
        imageRepository.save(image);
        project.updateThumbnailObjectKey(finalImageKey);
    }

    private void validateImageObjectKeys(Long projectId, ChatSendRequest request) {
        String allowedPrefix = "images/" + projectId + "/";
        if (request.referenceImageObjectKey() != null && !request.referenceImageObjectKey().isBlank()
                && !request.referenceImageObjectKey().startsWith(allowedPrefix)) {
            throw new ChatExceptionHandler(ChatErrorCode.INVALID_IMAGE_OBJECT_KEY);
        }
        if (request.maskImageObjectKey() != null && !request.maskImageObjectKey().isBlank()
                && !request.maskImageObjectKey().startsWith(allowedPrefix)) {
            throw new ChatExceptionHandler(ChatErrorCode.INVALID_IMAGE_OBJECT_KEY);
        }
    }

    private ChatMessage saveUserMessage(ChatRoom chatRoom, ChatSendRequest request) {
        List<String> attachedKeys = new ArrayList<>();
        if (request.referenceImageObjectKey() != null && !request.referenceImageObjectKey().isBlank()) {
            attachedKeys.add(request.referenceImageObjectKey());
        }
        if (request.maskImageObjectKey() != null && !request.maskImageObjectKey().isBlank()) {
            attachedKeys.add(request.maskImageObjectKey());
        }

        ChatMessage userMessage;
        if (!attachedKeys.isEmpty()) {
            userMessage = ChatMessage.createUserImageAttachment(
                    chatRoom, request.content(), String.join(",", attachedKeys));
        } else {
            userMessage = ChatMessage.createUserText(chatRoom, request.content());
        }
        return chatMessageRepository.save(userMessage);
    }

    private List<ChatMessage> buildAiContext(ChatRoom chatRoom) {
        List<ChatMessage> contextMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(
                chatRoom, PageRequest.of(0, CONTEXT_WINDOW_SIZE));
        contextMessages = new ArrayList<>(contextMessages);
        Collections.reverse(contextMessages);
        return contextMessages;
    }

    private String loadCurrentImageBase64(Project project, ChatSendRequest request) {
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
        return s3ImageLoader.loadAsBase64(currentImage.getImageObjectKey());
    }

    private String loadOptionalImageBase64(String objectKey) {
        if (objectKey != null && !objectKey.isBlank()) {
            return s3ImageLoader.loadAsBase64(objectKey);
        }
        return null;
    }

    private List<String> generateAndUploadConcepts(Project project, String prompt,
                                                    List<ChatMessage> contextMessages,
                                                    String currentImageBase64,
                                                    String referenceBase64, String maskBase64) {
        List<String> conceptBase64List = geminiChatClient.generateConceptImages(
                prompt, contextMessages, currentImageBase64, referenceBase64, maskBase64);

        List<String> conceptKeys = new ArrayList<>();
        for (String conceptBase64 : conceptBase64List) {
            String key = "images/" + project.getId() + "/chat/concept/" + UUID.randomUUID() + ".png";
            byte[] bytes = Base64.getDecoder().decode(conceptBase64);
            s3ImageUploader.upload(key, bytes);
            conceptKeys.add(key);
        }
        return conceptKeys;
    }

    private Project getProjectAndVerifyAccess(Long userId, Long projectId) {
        Project project = projectService.getProjectWithFolderById(projectId);
        folderManagerService.isUserWritable(userId, project.getFolder().getId());
        return project;
    }

}
