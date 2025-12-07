package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.FolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderService {
    private final UserService userService;
    private final ProjectManagerService projectManagerService;
    private final FolderRepository folderRepository;
    private final FolderManagerService folderManagerService;
    private final ImageService imageService;

    public static final int IMAGE_COUNT = 4;

    @Transactional
    public void addFolder(Long userId, Long projectId, FolderCreateRequest folderCreateRequest) {
        List<ProjectManager> managers = validate(userId, projectId);

        Project project = managers.get(0).getProject();
        Folder folder = Folder.create(folderCreateRequest.name(), project);
        folderRepository.save(folder);

        List<User> managerUsers = managers.stream()
                .map(ProjectManager::getUser)
                .toList();
        folderManagerService.addManagersByBulkInsert(managerUsers, folder);
    }

    public List<FolderResponse> getFolders(Long userId, Long projectId) {
        User user = userService.getUserByIdOrThrow(userId);
        List<FolderManager> managers = folderManagerService.getFolderManagersByProjectId(projectId);
        List<Folder> folders = folderManagerService.extractFolder(managers, user);

        Map<Long, List<String>> imagesByFolderId = imageService.getImagesByFolders(folders, IMAGE_COUNT);

        return folders.stream()
                .map(folder -> FolderResponse.create(
                            folder.getId(),
                            folder.getName(),
                            imagesByFolderId.getOrDefault(folder.getId(), List.of())
                ))
                .toList();
    }

    private List<ProjectManager> validate(Long userId, Long projectId) {
        User user = userService.getUserByIdOrThrow(userId);
        List<ProjectManager> managers = projectManagerService.getProjectManagersOrThrow(projectId);
        projectManagerService.existProjectManagersOrThrow(userId, managers);

        return managers;
    }


}
