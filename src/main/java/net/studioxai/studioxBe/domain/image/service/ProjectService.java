package net.studioxai.studioxBe.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectDashboardResponse;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.exception.ProjectErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ProjectExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    public List<ProjectDashboardResponse> getProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDashboardResponse::from)
                .toList();
    }
}
