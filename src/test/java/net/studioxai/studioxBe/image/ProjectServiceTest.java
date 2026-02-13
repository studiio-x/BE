package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.exception.ProjectExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.image.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private FolderRepository folderRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private FolderManagerService folderManagerService;
    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_success() {
        Long userId = 1L;
        Long projectId = 10L;

        Folder folder = mock(Folder.class);
        when(folder.getId()).thenReturn(100L);

        Project project = mock(Project.class);
        when(project.getFolder()).thenReturn(folder);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        // when
        projectService.deleteProject(userId, projectId);

        // then
        verify(folderManagerService)
                .isUserWritable(userId, 100L);

        verify(imageRepository)
                .deleteByProject(project);

        verify(projectRepository)
                .delete(project);
    }

    @Test
    @DisplayName("프로젝트가 존재하지 않으면 예외 발생")
    void deleteProject_notFound() {
        Long userId = 1L;
        Long projectId = 10L;

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(ProjectExceptionHandler.class, () ->
                projectService.deleteProject(userId, projectId)
        );
    }

    @Test
    @DisplayName("권한이 없으면 예외 발생")
    void deleteProject_noPermission() {
        Long userId = 1L;
        Long projectId = 10L;

        Folder folder = mock(Folder.class);
        when(folder.getId()).thenReturn(100L);

        Project project = mock(Project.class);
        when(project.getFolder()).thenReturn(folder);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        doThrow(new RuntimeException("권한 없음"))
                .when(folderManagerService)
                .isUserWritable(userId, 100L);

        assertThrows(RuntimeException.class, () ->
                projectService.deleteProject(userId, projectId)
        );

        verify(imageRepository, never())
                .deleteByProject(any());

        verify(projectRepository, never())
                .delete(any());
    }
}
