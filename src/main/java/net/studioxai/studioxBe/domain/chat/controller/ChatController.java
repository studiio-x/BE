package net.studioxai.studioxBe.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.chat.dto.request.ChatSendRequest;
import net.studioxai.studioxBe.domain.chat.dto.request.ConceptSelectRequest;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatHistoryResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatMessageResponse;
import net.studioxai.studioxBe.domain.chat.dto.response.ChatSendResponse;
import net.studioxai.studioxBe.domain.chat.entity.enums.ChatMode;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.domain.chat.service.ChatService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/v1/chat/{projectId}")
    public ChatHistoryResponse getChatHistory(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return chatService.getChatHistory(principal.userId(), projectId, page);
    }

    @PostMapping("/v1/chat/{projectId}/message/{mode}")
    public ChatSendResponse sendMessage(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @PathVariable ChatMode mode,
            @RequestBody @Valid ChatSendRequest request
    ) {
        return chatService.sendMessage(principal.userId(), projectId, mode, request);
    }

    @PostMapping("/v1/chat/{projectId}/concept/select")
    public ChatMessageResponse selectConcept(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @RequestBody @Valid ConceptSelectRequest request
    ) {
        return chatService.selectConcept(principal.userId(), projectId, request);
    }

    @GetMapping("/v1/chat/{projectId}/reference/presign")
    public S3Url issueReferencePresign(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId
    ) {
        return chatService.issueReferencePresign(principal.userId(), projectId);
    }

    @GetMapping("/v1/chat/{projectId}/mask/presign")
    public S3Url issueMaskPresign(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId
    ) {
        return chatService.issueMaskPresign(principal.userId(), projectId);
    }
}
