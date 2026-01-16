package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderManagerSerivce {

    private final FolderManagerRepository folderManagerRepository;
    private final UserService userService;
    private final FolderRepository folderRepository;

    @Transactional
    public void inviteManager(Long userId, Long folderId, FolderManagerAddRequest folderManagerAddRequest) {
        validatePermission(userId, folderId);

        userService.getUserByEmailOrThrow(folderManagerAddRequest.email());

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_NOT_FOUND)
        );

        createWritableManager(userId, folder);
    }

    @Transactional
    public void createRootManager(User user, Folder folder) {
        FolderManager rootManager = FolderManager.createRootManager(user, folder);
        folderManagerRepository.save(rootManager);
    }

    @Transactional
    public void createWritableManager(Long userId, Folder folder) {
        User user = userService.getUserByIdOrThrow(userId);
        FolderManager writerManager = FolderManager.createWriter(user, folder);
        folderManagerRepository.save(writerManager);
    }

    public void validatePermission(Long userId, Long folderId) {
        boolean writable = folderManagerRepository
                .existsByFolderIdAndUserIdAndPermissionIn(folderId, userId,
                        List.of(Permission.WRITE, Permission.OWNER));

        if (!writable) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY);
        }
    }
}
