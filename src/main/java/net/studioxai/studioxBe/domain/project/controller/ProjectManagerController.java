package net.studioxai.studioxBe.domain.project.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.project.dto.MyProjectResponse;
import net.studioxai.studioxBe.domain.project.dto.ProjectUserResponse;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectManagerController {
    private final ProjectManagerService projectManagerService;

    @GetMapping("/v1/project")
    public List<MyProjectResponse> myProjectList(
            @AuthenticationPrincipal JwtUserPrincipal jwtUserPrincipal
    ) {
        return projectManagerService.getMyProjectList(jwtUserPrincipal.userId());
    }

    @GetMapping("/v1/project/{projectId}/managers")
    public List<ProjectUserResponse> projectManagerList(
            @AuthenticationPrincipal JwtUserPrincipal jwtUserPrincipal,
            @PathVariable Long projectId
    ) {
        return projectManagerService.getProjectManagerList(jwtUserPrincipal.userId(), projectId);
    }
}
