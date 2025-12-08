package net.studioxai.studioxBe.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.project.dto.MyProjectResponse;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerExceptionHandler;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerErrorCode;
import net.studioxai.studioxBe.domain.project.repository.ProjectManagerRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectManagerService {
    private final ProjectManagerRepository projectManagerRepository;
    private final UserService userService;

    public void existProjectManagersOrThrow(Long userId, List<ProjectManager> managers) {
        boolean exists = managers.stream().anyMatch(manager -> manager.getUser().getId().equals(userId));
        if (!exists) {
            throw new ProjectMangerExceptionHandler(ProjectMangerErrorCode.USER_NO_PROJECT_AUTHORITY);
        }
    }

    public List<ProjectManager> getProjectManagersOrThrow(Long projectId) {
        List<ProjectManager> managers = projectManagerRepository.findByProjectId(projectId);
        if (managers.isEmpty()) {
            throw new ProjectMangerExceptionHandler(ProjectMangerErrorCode.PROJECT_NOT_FOUND);
        }
        return managers;
    }

    @Transactional
    public void addManager(Project project, User user, boolean isAdmin) {
        ProjectManager projectManager = ProjectManager.create(project, user, isAdmin);
        projectManagerRepository.save(projectManager);
    }

    public List<MyProjectResponse> getMyProjectList(Long userId) {
        User user = userService.getUserByIdOrThrow(userId);

        List<MyProjectResponse> responses = projectManagerRepository.findByUser(user);
        return responses.stream().sorted(Comparator.comparing(MyProjectResponse::isAdmin).reversed()).toList();
    }
}
