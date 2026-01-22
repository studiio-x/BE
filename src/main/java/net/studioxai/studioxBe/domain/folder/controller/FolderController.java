package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FolderResponse;
import net.studioxai.studioxBe.domain.folder.dto.response.RootFolderResponse;
import net.studioxai.studioxBe.domain.folder.service.FolderService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @PostMapping("/v1/folder/{rootFolderId}")
    public void folderAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long rootFolderId,
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        folderService.createSubFolder(principal.userId(), rootFolderId, folderCreateRequest);
    }

    @GetMapping("/v1/folder")
    public List<RootFolderResponse> myfolders(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return folderService.findFolders(principal.userId());
    }

}
