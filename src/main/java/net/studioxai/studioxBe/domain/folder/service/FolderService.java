package net.studioxai.studioxBe.domain.folder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.dto.FoldersDto;
import net.studioxai.studioxBe.domain.folder.dto.projection.RootFolderProjection;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.RootFolderDto;
import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.response.FoldersResponse;
import net.studioxai.studioxBe.domain.folder.dto.response.MyFolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.*;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.dto.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final FolderManagerBulkRepository folderManagerBulkRepository;
    private final FolderManagerRepository folderManagerRepository;
    private final ClosureFolderMoveRepository closureFolderMoveRepository;

    public FoldersResponse findFoldersByFolderId(Long userId, Long folderId, Sort.Direction sort, int pageNum, int limit) {
        folderManagerService.isUserReadable(userId, folderId);
        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        PageRequest pageRequest = PageRequest.of(pageNum, limit, Sort.by(sort, "createdAt"));

        Page<Folder> folders = folderRepository.findByParentFolder(folder, pageRequest);
        List<FoldersDto> folderDtos = folders.stream()
                .map(f -> FoldersDto.create(
                        f.getId(),
                        f.getName(),
                        null
                ))
                .toList();

        PageInfo pageInfo = PageInfo.of(pageNum, limit, folders.getTotalPages(), folders.getTotalElements());

        return FoldersResponse.create(folderDtos, pageInfo);
    }

    @Transactional
    public void changeLinkMode(Long userId, Long folderId) {
        folderManagerService.isUserWritable(userId, folderId);
        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        folder.updateLinkMode();
        folderRepository.flush();

        List<FolderManagerDto> managers = folderManagerService.getManagers(folder.getParentFolder().getId());

        if (!folder.getLinkMode().isLink()) {
            folderManagerBulkRepository.upsertManagersForFolder(folderId, managers);
            folderRepository.updateAclRootForSubtree(folderId);
        } else {

            folderManagerBulkRepository.deleteManagersForFolder(folderId, managers);
            folderRepository.updateAclRootForSubtreeToParentAclRoot(folderId);
        }


    }

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

    public MyFolderResponse findMyFoldes(Long userId) {
        List<RootFolderProjection> rows = closureFolderRepository.findMyFolders(userId);

        List<RootFolderDto> myProject = rows.stream()
                .filter(r -> r.getIsOwner() == 1)
                .map(r -> RootFolderDto.create(r.getFolderId(), r.getName()))
                .toList();

        List<RootFolderDto> sharedProjects = rows.stream()
                .filter(r -> r.getIsOwner() == 0)
                .map(r -> RootFolderDto.create(r.getFolderId(), r.getName()))
                .toList();

        return MyFolderResponse.create(myProject, sharedProjects);
    }

    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        folderManagerService.isUserWritable(userId, folderId);

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        List<Long> subFolderIds = closureFolderRepository.findDescendantFolderIds(folderId);

        if (!subFolderIds.isEmpty()) {
            folderManagerRepository.deleteAllByFolderIds(subFolderIds);
        }

        closureFolderRepository.deleteEdgesByAncestor(folder.getId());
        folderRepository.deleteAllByIdsIn(subFolderIds);

        // TODO: 하위 프로젝트 (cutOut Image 삭제)

    }

    @Transactional
    public void updateFolderName(Long userId, Long folderId, FolderCreateRequest folderCreateRequest) {
        folderManagerService.isUserWritable(userId, folderId);

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        folder.updateName(folderCreateRequest.name());
    }

    @Transactional
    public void moveFolder(Long userId, Long targetFolderId, Long destinationFolderId) {
        folderManagerService.isUserWritable(userId, targetFolderId);
        folderManagerService.isUserWritable(userId, destinationFolderId);

        Folder targetFolder = folderRepository.findById(targetFolderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        Folder destinationFolder = folderRepository.findById(destinationFolderId).orElseThrow(
                () -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND)
        );

        targetFolder.move(destinationFolder);
        moveSubtree(targetFolderId, destinationFolderId);
    }

    private void moveSubtree(Long targetFolderId, Long destinationFolderId) {
        if (closureFolderMoveRepository.existsPath(targetFolderId, destinationFolderId)) {
            throw new FolderExceptionHandler(FolderErrorCode.INVALID_FOLDER_HIERARCHY_MOVE);
        }

        if (targetFolderId.equals(destinationFolderId)) {
            throw new FolderExceptionHandler(FolderErrorCode.INVALID_MOVE_FOLDER_TO_ITSELF);
        }

        closureFolderMoveRepository.deleteOldAncestorLinks(targetFolderId);
        closureFolderMoveRepository.insertNewAncestorLinks(targetFolderId, destinationFolderId);
    }


}
