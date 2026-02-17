package net.studioxai.studioxBe.domain.image.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.request.ProjectTitleUpdateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectMoveResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectTitleUpdateResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectsResponse;
import net.studioxai.studioxBe.domain.image.service.ProjectService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/v1/project/{folderId}")
    public ProjectsResponse projectsByFolderId(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long folderId,
            @RequestParam(required = false, defaultValue = "desc") Sort.Direction sort,
            @RequestParam(required = true) int pageNum,
            @RequestParam(required = true) int limit
    ) {
        return projectService.getProjectsByFolderId(principal.userId(), folderId, sort, pageNum, limit);
    }

    @PatchMapping("/v1/project/{projectId}/title")
    public ProjectTitleUpdateResponse updateProjectTitle(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @RequestBody ProjectTitleUpdateRequest request
    ) {
        return projectService.updateProjectTitle(principal.userId(), projectId, request.title()
        );
    }

    @PatchMapping("/v1/project/{projectId}/{destinationFolderId}")
    public ProjectMoveResponse moveProject(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId,
            @PathVariable Long destinationFolderId
    ) {
        return projectService.moveProject(principal.userId(), projectId, destinationFolderId);
    }


    @DeleteMapping("/v1/project/{projectId}")
    public void deleteProject(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long projectId
    ) {
        projectService.deleteProject(principal.userId(), projectId);
    }


}
