package net.studioxai.studioxBe.folder;

import net.studioxai.studioxBe.domain.folder.dto.FolderResponse;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.folder.service.FolderService;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProjectManagerService projectManagerService;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FolderManagerService folderManagerService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private FolderService folderService;

    @Test
    @DisplayName("[addFolder] 프로젝트 매니저가 폴더를 추가하면 폴더 저장 및 매니저 일괄 등록이 수행된다")
    void addFolder_success() {
        // given
        Long userId = 1L;
        Long projectId = 10L;
        FolderCreateRequest request = new FolderCreateRequest("새 폴더");

        User user = new User();
        Project project = mock(Project.class);

        User managerUser1 = new User();
        User managerUser2 = new User();

        ProjectManager pm1 = mock(ProjectManager.class);
        ProjectManager pm2 = mock(ProjectManager.class);

        List<ProjectManager> managers = List.of(pm1, pm2);

        given(userService.getUserByIdOrThrow(userId)).willReturn(user);
        given(projectManagerService.getProjectMangersOrThrow(projectId)).willReturn(managers);

        given(pm1.getProject()).willReturn(project);
        given(pm1.getUser()).willReturn(managerUser1);
        given(pm2.getUser()).willReturn(managerUser2);

        // when
        folderService.addFolder(userId, projectId, request);

        // then
        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(folderCaptor.capture());

        Folder savedFolder = folderCaptor.getValue();
        assertThat(savedFolder.getName()).isEqualTo(request.name());
        assertThat(savedFolder.getProject()).isEqualTo(project);

        verify(projectManagerService).existProjectMangersOrThrow(userId, managers);

        List<User> expectedManagerUsers = List.of(managerUser1, managerUser2);
        verify(folderManagerService).addManagersByBulkInsert(expectedManagerUsers, savedFolder);
    }

    @Test
    @DisplayName("[getFolders] 유저가 매니징하는 폴더 목록과 폴더별 대표 이미지가 반환된다")
    void getFolders_success() {
        // given
        Long userId = 1L;
        Long projectId = 10L;

        User user = mock(User.class);

        FolderManager fm1 = mock(FolderManager.class);
        FolderManager fm2 = mock(FolderManager.class);
        List<FolderManager> folderManagers = List.of(fm1, fm2);

        Folder folder1 = mock(Folder.class);
        Folder folder2 = mock(Folder.class);

        Long folderId1 = 100L;
        Long folderId2 = 200L;

        given(folder1.getId()).willReturn(folderId1);
        given(folder1.getName()).willReturn("폴더1");
        given(folder2.getId()).willReturn(folderId2);
        given(folder2.getName()).willReturn("폴더2");

        List<Folder> folders = List.of(folder1, folder2);

        given(userService.getUserByIdOrThrow(userId)).willReturn(user);
        given(folderManagerService.getFolderManagersByProjectId(projectId)).willReturn(folderManagers);
        given(folderManagerService.extractFolder(folderManagers, user)).willReturn(folders);

        Map<Long, List<String>> imagesByFolderId = Map.of(
                folderId1, List.of("img1.png", "img2.png")
        );
        given(imageService.getImagesByFolders(folders, FolderService.IMAGE_COUNT))
                .willReturn(imagesByFolderId);

        // when
        List<FolderResponse> result = folderService.getFolders(userId, projectId);

        // then
        assertThat(result).hasSize(2);

        FolderResponse resp1 = result.get(0);
        FolderResponse resp2 = result.get(1);

        assertThat(resp1.folderId()).isEqualTo(folderId1);
        assertThat(resp1.name()).isEqualTo("폴더1");
        assertThat(resp1.images()).containsExactly("img1.png", "img2.png");

        assertThat(resp2.folderId()).isEqualTo(folderId2);
        assertThat(resp2.name()).isEqualTo("폴더2");
        assertThat(resp2.images()).isEmpty();

        verify(imageService).getImagesByFolders(folders, FolderService.IMAGE_COUNT);
    }
}