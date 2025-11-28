package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FolderManagerService {
    private final FolderManagerRepository folderManagerRepository;

    @Transactional
    public void addManager(User user, Folder folder) {
        FolderManager folderManager = FolderManager.create(user, folder);
        folderManagerRepository.save(folderManager);
    }
}
