package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.RootFolderDto;
import net.studioxai.studioxBe.domain.folder.dto.response.MyFolderResponse;
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

    // TODO: 하위 폴더 조회 API


    @DeleteMapping("/v1/folder/{folderId}")
    public void folderDelete(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId
    ) {
        folderService.deleteFolder(principal.userId(), folderId);
    }

    @PutMapping("/v1/folder/{folderId}/name")
    public void folderNameUpdate(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        folderService.updateFolderName(principal.userId(), folderId, folderCreateRequest);
    }

    // TODO: 폴더 이동 API



    @PostMapping("/v1/folder/{rootFolderId}")
    public void folderAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long rootFolderId,
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        folderService.createSubFolder(principal.userId(), rootFolderId, folderCreateRequest);
    }

    @GetMapping("/v1/folder")
    public MyFolderResponse myfolders(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return folderService.findFolders(principal.userId());
    }

    @PutMapping("/v1/folder/{folderId}/link")
    public void folderUnlinked(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId
    ) {
        folderService.changeLinkMode(principal.userId(), folderId);
    }



}
