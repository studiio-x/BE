package net.studioxai.studioxBe.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectDashboardResponse;
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

    public List<ProjectDashboardResponse> getProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDashboardResponse::from)
                .toList();
    }
}
