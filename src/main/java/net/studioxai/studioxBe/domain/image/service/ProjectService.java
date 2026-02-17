package net.studioxai.studioxBe.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.dto.ProjectsDto;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectMoveResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectTitleUpdateResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ProjectsResponse;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.exception.ProjectErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ProjectExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.global.dto.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;

    private final FolderManagerService folderManagerService;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    public Project getProjectWithFolderById(Long projectId) {
        return projectRepository.findWithFolderById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    public ProjectsResponse getProjectsByFolderId(Long userId, Long folderId, Sort.Direction sort, int pageNum, int limit) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND));

        folderManagerService.isUserReadable(userId, folderId);

        PageRequest pageRequest = PageRequest.of(pageNum, limit, Sort.by(sort, "createdAt"));

        Page<Project> projects = projectRepository.findByFolder(folder, pageRequest);

        List<ProjectsDto> projectDtos =
                projects.stream()
                        .map(p -> ProjectsDto.create(
                                p.getId(),
                                p.getTitle(),
                                p.getThumbnailObjectKey()
                        ))
                        .toList();

        PageInfo pageInfo = PageInfo.of(
                pageNum,
                limit,
                projects.getTotalPages(),
                projects.getTotalElements()
        );

        return ProjectsResponse.create(projectDtos, pageInfo);
    }

    @Transactional
    public ProjectTitleUpdateResponse updateProjectTitle(Long userId, Long projectId, String title) {

        Project project = getProjectWithFolderById(projectId);

        folderManagerService.isUserWritable(userId, project.getFolder().getId());

        project.updateTitle(title);

        return ProjectTitleUpdateResponse.of(project);
    }

    @Transactional
    public ProjectMoveResponse moveProject(Long userId, Long projectId, Long destinationFolderId) {

        Project project = getProjectWithFolderById(projectId);

        folderManagerService.isUserWritable(userId, project.getFolder().getId());
        Folder destinationFolder = folderRepository.findById(destinationFolderId)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND));
        folderManagerService.isUserWritable(userId, destinationFolder.getId());

        project.moveTo(destinationFolder);

        return ProjectMoveResponse.of(project);
    }


    @Transactional
    public void deleteProject(Long userId, Long projectId) {

        Project project = getProjectWithFolderById(projectId);

        folderManagerService.isUserWritable(userId, project.getFolder().getId());

        imageRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

}
