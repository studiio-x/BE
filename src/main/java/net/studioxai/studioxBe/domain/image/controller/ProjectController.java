package net.studioxai.studioxBe.domain.image.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectDashboardResponse;
import net.studioxai.studioxBe.domain.image.service.ProjectService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/v1/project/dashboard")
    public ResponseEntity<List<ProjectDashboardResponse>> getProjects(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjects());
    }
}
