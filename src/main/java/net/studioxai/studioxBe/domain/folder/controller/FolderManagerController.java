package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FolderManagersResponse;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderManagerController {
    private final FolderManagerService folderManagerService;

    @PostMapping("/v1/folder/manager/{folderId}")
    public void folderManagerAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @RequestBody @Valid FolderManagerAddRequest folderManagerAddRequest
    ) {
        folderManagerService.inviteManager(principal.userId(), folderId, folderManagerAddRequest);
    }

    @PutMapping("/v1/folder/manager/{folderId}/{targetUserId}")
    public void folderManagerUpdate(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @PathVariable Long targetUserId,
            @RequestParam Permission permission
    ) {
        folderManagerService.updatePermission(principal.userId(), targetUserId, folderId, permission);
    }

    @GetMapping("/v1/folder/manager/{folderId}")
    public FolderManagersResponse folderManagers(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId
    ) {
        return folderManagerService.getManagers(principal.userId(), folderId);
    }


}
