package net.studioxai.studioxBe.domain.folder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerSerivce;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderManagerController {
    private final FolderManagerSerivce folderManagerService;

//    @GetMapping("/v1/project")
//    public List<MyProjectResponse> myProjectList(
//            @AuthenticationPrincipal JwtUserPrincipal jwtUserPrincipal
//    ) {
//        return projectManagerService.getMyProjectList(jwtUserPrincipal.userId());
//    }

    @PostMapping("/v1/folder/{folderId}/manager")
    public void folderManagerAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @RequestBody @Valid FolderManagerAddRequest folderManagerAddRequest
    ) {
        folderManagerService.inviteManager(principal.userId(), folderId, folderManagerAddRequest);
    }
}
