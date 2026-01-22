package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.RootFolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.ClosureFolderInsertRepository;
import net.studioxai.studioxBe.domain.folder.repository.ClosureFolderRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderManagerService folderManagerService;
    private final ClosureFolderInsertRepository closureFolderInsertRepository;
    private final ClosureFolderRepository closureFolderRepository;

    @Transactional
    public Folder createRootFolder(String folderName, User user) {
        Folder rootFolder = Folder.createRoot(folderName);
        folderRepository.saveAndFlush(rootFolder);
        folderManagerService.createRootManager(user, rootFolder);
        closureFolderInsertRepository.insertClosureForNewFolder(null, rootFolder.getId());
        rootFolder.updateRootAclId();
        return rootFolder;
    }

    @Transactional
    public void createSubFolder(Long userId, Long parentFolderId, FolderCreateRequest folderCreateRequest) {
        Folder parentFolder = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.PARENT_REQUIRED));

        folderManagerService.isUserWritable(userId, parentFolderId);

        Folder subFolder = Folder.createSub(folderCreateRequest.name(), parentFolder);
        folderRepository.saveAndFlush(subFolder);
        closureFolderInsertRepository.insertClosureForNewFolder(parentFolder.getId(), subFolder.getId());
    }

    public List<RootFolderResponse> findFolders(Long userId) {
        return closureFolderRepository.findMyFolders(userId).stream()
                .map(p -> RootFolderResponse.create(
                        p.getFolderId(),
                        p.getName()
                ))
                .toList();
    }

}
