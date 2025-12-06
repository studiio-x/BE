package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.service.FolderService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @PostMapping("/v1/folder/{projectId}")
    public void folderAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        folderService.addFolder(principal.userId(), projectId, folderCreateRequest);
    }
}
