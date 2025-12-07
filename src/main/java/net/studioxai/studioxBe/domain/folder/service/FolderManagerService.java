package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerBulkRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderManagerService {
    private final FolderManagerRepository folderManagerRepository;
    private final FolderManagerBulkRepository folderManagerBulkRepository;
    private final UserService userService;

    public void existFolderMangersOrThrow(Long userId, List<FolderManager> managers) {
        boolean exists = managers.stream().anyMatch(manager -> manager.getUser().getId().equals(userId));
        if (!exists) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY);
        }
    }

    public List<FolderManager> getFolderMangersOrThrow(Long folderId) {
        List<FolderManager> managers = folderManagerRepository.findByFolderId(folderId);
        if (managers.isEmpty()) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_NOT_FOUND);
        }
        return managers;
    }

    public List<FolderManager> getFolderManagersByProjectId(Long projectId) {
        List<FolderManager> managers = folderManagerRepository.findByProjectId(projectId);

        if (managers.isEmpty()) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.PROJECT_FOLDER_NOT_FOUND);
        }
        return managers;
    }

    public List<Folder> extractFolder(List<FolderManager> folderManagers, User user) {
        return folderManagers.stream()
                .filter(folderManager -> folderManager.getUser().getId().equals(user.getId()))
                .map(FolderManager::getFolder)
                .toList();
    }


    @Transactional
    public void addManager(Long userId, Long folderId, FolderManagerAddRequest folderManagerAddRequest) {
        List<FolderManager> managers = getFolderMangersOrThrow(folderId);
        existFolderMangersOrThrow(userId, managers);
        Folder folder = managers.get(0).getFolder();

        User user = userService.getUserByEmailOrThrow(folderManagerAddRequest.email());

        FolderManager folderManager = FolderManager.create(user, folder);
        folderManagerRepository.save(folderManager);
    }

    @Transactional
    public void addManagersByBulkInsert(List<User> users, Folder folder) {
        folderManagerBulkRepository.saveAllByBulk(users, folder);
    }
}
