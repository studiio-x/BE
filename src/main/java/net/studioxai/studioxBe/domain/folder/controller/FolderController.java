package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FoldersResponse;
import net.studioxai.studioxBe.domain.folder.dto.response.MyFolderResponse;
import net.studioxai.studioxBe.domain.folder.service.FolderService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @GetMapping("/v1/folder/{folderId}")
    public FoldersResponse foldersByFolderId(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @RequestParam(required = true, defaultValue = "desc") Sort.Direction sort,
            @RequestParam(required = true) int pageNum,
            @RequestParam(required = true) int limit
    ) {
        return folderService.getFoldersByFolderId(principal.userId(), folderId, sort, pageNum, limit);
    }


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

    @PutMapping("/v1/folder/{targetFolderId}/{destinationFolderId}")
    public void folderAclRootUpdate(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long targetFolderId,
            @PathVariable Long destinationFolderId
    ) {
        folderService.moveFolder(principal.userId(), targetFolderId, destinationFolderId);
    }

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
        return folderService.getMyFolders(principal.userId());
    }

    @PutMapping("/v1/folder/{folderId}/link")
    public void folderUnlinked(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId
    ) {
        folderService.changeLinkMode(principal.userId(), folderId);
    }



}
