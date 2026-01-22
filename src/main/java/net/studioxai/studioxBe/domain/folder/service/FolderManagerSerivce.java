package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.PermissionDto;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FolderManagersResponse;
import net.studioxai.studioxBe.domain.folder.dto.response.RootFolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.ClosureFolderRepository;
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
    private final ClosureFolderRepository closureFolderRepository;

    public FolderManagersResponse getManagers(Long userId, Long folderId) {
        List<FolderManagerDto> folderManagers = getManagers(folderId);
        if (folderManagers.isEmpty()) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_NOT_FOUND);
        }

        FolderManagerDto me = folderManagers.stream()
                .filter(m -> m.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new FolderManagerExceptionHandler(
                        FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY
                ));

        PermissionDto myPermission = PermissionDto.create(me.permission());

        return FolderManagersResponse.create(myPermission, folderManagers);
    }

    @Transactional
    public void updatePermission(Long actorUserId, Long targetUserId, Long folderId) {
        isUserWritable(actorUserId, folderId);

        FolderManager folderManager = folderManagerRepository.findByFolderIdAndUserId(folderId, targetUserId).orElseThrow(
                () -> new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_PARENT_RELATION_EXISTS)
        );

        folderManager.updateDirectPermission();

    }

    @Transactional
    public void inviteManager(Long userId, Long folderId, FolderManagerAddRequest folderManagerAddRequest) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_NOT_FOUND)
        );

        isUserWritable(userId, folderId);

        userService.getUserByEmailOrThrow(folderManagerAddRequest.email());

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

    // TODO: 상위 폴더 manager도 한꺼번에 갖고오기
    private List<FolderManagerDto> getManagers(Long folderId) {
        return folderManagerRepository.findByFolderId(folderId);
    }

    public List<RootFolderResponse> getFolders(Long userId) {
        return folderManagerRepository.findByUserId(userId);
    }

    public void isUserWritable(Long userId, Long folderId) {
        if (!getPermission(userId, folderId).isWritable()) {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY);
        }
    }

    private Permission getPermission(Long userId, Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderManagerExceptionHandler(FolderManagerErrorCode.FOLDER_NOT_FOUND)
        );

        return closureFolderRepository.findPermission(folderId, folder.getAclRootFolderId(), userId).orElseThrow(
                () -> new FolderManagerExceptionHandler(FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY)
        );
    }

}
