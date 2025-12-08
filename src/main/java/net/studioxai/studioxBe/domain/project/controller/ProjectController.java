package net.studioxai.studioxBe.domain.project.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.project.dto.ProjectCreateRequest;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.service.ProjectService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/v1/project")
    public void projectAdd(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody ProjectCreateRequest projectCreateRequest
    ) {
        projectService.createProject(principal.userId(), projectCreateRequest);
    }

}
