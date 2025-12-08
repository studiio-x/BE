package net.studioxai.studioxBe.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.project.dto.ProjectCreateRequest;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ProjectManagerService projectManagerService;

    @Transactional
    public void createProject (Long userId, ProjectCreateRequest projectCreateRequest) {
        User user = userService.getUserByIdOrThrow(userId);
        addProject(user, projectCreateRequest.name(), true);
    }

    @Transactional
    public void addProject(User user, String name, boolean isAdmin) {
        Project project = Project.create(name);
        projectRepository.save(project);

        projectManagerService.addManager(project, user, isAdmin);
    }
}
