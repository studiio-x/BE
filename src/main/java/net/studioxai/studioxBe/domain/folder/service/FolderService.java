package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderManagerSerivce folderManagerSerivce;

    @Transactional
    public Folder createRootFolder(String folderName) {
        Folder rootFolder = Folder.createRoot(folderName);
        folderRepository.save(rootFolder);
        return rootFolder;
    }

    @Transactional
    public void createSubFolder(Long userId, Long parentFolderId, FolderCreateRequest folderCreateRequest) {
        folderManagerSerivce.validatePermission(userId, parentFolderId);

        Folder parentFolder = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.PARENT_REQUIRED));

        Folder subFolder = Folder.createSub(folderCreateRequest.name(), parentFolder);
        folderManagerSerivce.createWritableManager(userId, subFolder);
    }

}
