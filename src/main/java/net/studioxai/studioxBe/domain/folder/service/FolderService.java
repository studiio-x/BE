package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderService {
    private final UserService userService;
    private final ProjectManagerService projectManagerService;
    private final FolderRepository folderRepository;
    private final FolderManagerService folderManagerService;

    @Transactional
    public void addFolder(Long userId, Long projectId, FolderCreateRequest folderCreateRequest) {
        User user = userService.getUserByIdOrThrow(userId);
        List<ProjectManager> managers = projectManagerService.getProjectMangersOrThrow(projectId);
        projectManagerService.existProjectMangersOrThrow(userId, managers);

        Project project = managers.get(0).getProject();
        Folder folder = Folder.create(folderCreateRequest.name(), project);
        folderRepository.save(folder);

        List<User> managerUsers = managers.stream()
                .map(ProjectManager::getUser)
                .toList();
        folderManagerService.addManagersByBulkInsert(managerUsers, folder);
    }

}
