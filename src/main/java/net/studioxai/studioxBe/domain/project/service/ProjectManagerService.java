package net.studioxai.studioxBe.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerExceptionHandler;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerErrorCode;
import net.studioxai.studioxBe.domain.project.repository.ProjectManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectManagerService {
    private final ProjectManagerRepository projectManagerRepository;

    public void existProjectMangersOrThrow(Long userId, List<ProjectManager> managers) {
        boolean exists = managers.stream().anyMatch(manager -> manager.getUser().getId().equals(userId));
        if (!exists) {
            throw new ProjectMangerExceptionHandler(ProjectMangerErrorCode.USER_NO_PROJECT_AUTHORITY);
        }
    }

    public List<ProjectManager> getProjectMangersOrThrow(Long projectId) {
        List<ProjectManager> managers = projectManagerRepository.findByProjectId(projectId);
        if (managers.isEmpty()) {
            throw new ProjectMangerExceptionHandler(ProjectMangerErrorCode.PROJECT_NOT_FOUND);
        }
        return managers;
    }
}
