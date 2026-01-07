package net.studioxai.studioxBe.folder;

import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerBulkRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FolderManagerServiceTest {

    @Mock
    private FolderManagerRepository folderManagerRepository;

    @Mock
    private FolderManagerBulkRepository folderManagerBulkRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FolderManagerService folderManagerService;

    @Test
    @DisplayName("[existFolderMangersOrThrow] 사용자가 매니저 목록에 있으면 예외 없이 통과한다")
    void existFolderManagers_ok() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        given(user.getId()).willReturn(userId);

        FolderManager manager = mock(FolderManager.class);
        given(manager.getUser()).willReturn(user);

        List<FolderManager> managers = List.of(manager);

        // when & then
        assertThatCode(() ->
                folderManagerService.existFolderMangersOrThrow(userId, managers)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[existFolderMangersOrThrow] 사용자가 매니저 목록에 없으면 USER_NO_FOLDER_AUTHORITY 예외 발생")
    void existFolderManagers_notFound() {
        // given
        Long userId = 1L;

        User otherUser = mock(User.class);
        given(otherUser.getId()).willReturn(2L);

        FolderManager manager = mock(FolderManager.class);
        given(manager.getUser()).willReturn(otherUser);

        List<FolderManager> managers = List.of(manager);

        // when & then
        assertThatThrownBy(() ->
                folderManagerService.existFolderMangersOrThrow(userId, managers)
        )
                .isInstanceOf(FolderManagerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(FolderManagerErrorCode.USER_NO_FOLDER_AUTHORITY);
    }

    @Test
    @DisplayName("[getFolderMangersOrThrow] 폴더 매니저가 존재하면 리스트를 반환한다")
    void getFolderManagers_ok() {
        // given
        Long folderId = 10L;
        FolderManager manager = mock(FolderManager.class);
        given(folderManagerRepository.findByFolderId(folderId))
                .willReturn(List.of(manager));

        // when
        List<FolderManager> result = folderManagerService.getFolderMangersOrThrow(folderId);

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(manager);
    }

    @Test
    @DisplayName("[getFolderMangersOrThrow] 폴더 매니저가 없으면 FOLDER_NOT_FOUND 예외 발생")
    void getFolderManagers_empty() {
        // given
        Long folderId = 10L;
        given(folderManagerRepository.findByFolderId(folderId))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() ->
                folderManagerService.getFolderMangersOrThrow(folderId)
        )
                .isInstanceOf(FolderManagerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(FolderManagerErrorCode.FOLDER_NOT_FOUND);
    }


    @Test
    @DisplayName("[getFolderManagersByProjectId] 프로젝트에 속한 폴더 매니저가 있으면 리스트를 반환한다")
    void getFolderManagersByProjectId_ok() {
        Long projectId = 1L;
        FolderManager manager = mock(FolderManager.class);

        given(folderManagerRepository.findByProjectId(projectId))
                .willReturn(List.of(manager));

        List<FolderManager> result =
                folderManagerService.getFolderManagersByProjectId(projectId);

        assertThat(result)
                .hasSize(1)
                .containsExactly(manager);
    }

    @Test
    @DisplayName("[getFolderManagersByProjectId] 프로젝트에 폴더 매니저가 없으면 PROJECT_FOLDER_NOT_FOUND 예외 발생")
    void getFolderManagersByProjectId_empty() {
        Long projectId = 1L;
        given(folderManagerRepository.findByProjectId(projectId))
                .willReturn(List.of());

        assertThatThrownBy(() ->
                folderManagerService.getFolderManagersByProjectId(projectId)
        )
                .isInstanceOf(FolderManagerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(FolderManagerErrorCode.PROJECT_FOLDER_NOT_FOUND);
    }

    @Test
    @DisplayName("[extractFolder] 해당 유저가 매니징하는 폴더만 필터링해서 반환한다")
    void extractFolder_filterByUser() {
        // given
        User targetUser = mock(User.class);
        given(targetUser.getId()).willReturn(1L);

        User otherUser = mock(User.class);
        given(otherUser.getId()).willReturn(2L);

        Folder folder1 = mock(Folder.class);
        Folder folder2 = mock(Folder.class);

        FolderManager m1 = mock(FolderManager.class);
        given(m1.getUser()).willReturn(targetUser);
        given(m1.getFolder()).willReturn(folder1);

        FolderManager m2 = mock(FolderManager.class);
        given(m2.getUser()).willReturn(otherUser);

        List<FolderManager> managers = List.of(m1, m2);

        // when
        List<Folder> result = folderManagerService.extractFolder(managers, targetUser);

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(folder1);
    }

    @Test
    @DisplayName("[addManager] 요청자가 해당 폴더 매니저이고, 추가 대상 유저가 아직 아니면 새 FolderManager를 저장한다")
    void addManager_success() {
        // given
        Long requesterId = 1L;
        Long folderId = 100L;

        Folder folder = mock(Folder.class);
        User requester = mock(User.class);
        given(requester.getId()).willReturn(requesterId);

        FolderManager existingManager = mock(FolderManager.class);
        given(existingManager.getUser()).willReturn(requester);
        given(existingManager.getFolder()).willReturn(folder);

        given(folderManagerRepository.findByFolderId(folderId))
                .willReturn(List.of(existingManager));

        String targetEmail = "test@studiox.ai";
        User targetUser = mock(User.class);
        given(targetUser.getId()).willReturn(2L);

        given(userService.getUserByEmailOrThrow(targetEmail))
                .willReturn(targetUser);

        FolderManagerAddRequest request =
                new FolderManagerAddRequest(targetEmail);

        // when
        folderManagerService.addManager(requesterId, folderId, request);

        // then
        ArgumentCaptor<FolderManager> captor =
                ArgumentCaptor.forClass(FolderManager.class);

        verify(folderManagerRepository, times(1))
                .save(captor.capture());

        FolderManager saved = captor.getValue();
        assertThat(saved).isNotNull();
    }

    @Test
    @DisplayName("[addManager] 추가 대상 유저가 이미 폴더 매니저면 USER_ALREADY_FOLDER_MANAGER 예외 발생")
    void addManager_alreadyManager() {
        Long requesterId = 1L;
        Long folderId = 100L;

        Folder folder = mock(Folder.class);

        User requester = mock(User.class);
        given(requester.getId()).willReturn(requesterId);

        User targetUser = mock(User.class);
        given(targetUser.getId()).willReturn(2L);

        FolderManager m1 = mock(FolderManager.class);
        given(m1.getUser()).willReturn(requester);
        given(m1.getFolder()).willReturn(folder);

        FolderManager m2 = mock(FolderManager.class);
        given(m2.getUser()).willReturn(targetUser);

        given(folderManagerRepository.findByFolderId(folderId))
                .willReturn(List.of(m1, m2));

        String targetEmail = "target@studiox.ai";
        given(userService.getUserByEmailOrThrow(targetEmail))
                .willReturn(targetUser);

        FolderManagerAddRequest request =
                new FolderManagerAddRequest(targetEmail);

        // when & then
        assertThatThrownBy(() ->
                folderManagerService.addManager(requesterId, folderId, request)
        )
                .isInstanceOf(FolderManagerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(FolderManagerErrorCode.USER_ALREADY_FOLDER_MANAGER);

        verify(folderManagerRepository, never()).save(any());
    }

    @Test
    @DisplayName("[addManagersByBulkInsert] bulk repository에 그대로 위임한다")
    void addManagersByBulkInsert_delegate() {
        List<User> users = List.of(mock(User.class), mock(User.class));
        Folder folder = mock(Folder.class);

        folderManagerService.addManagersByBulkInsert(users, folder);

        verify(folderManagerBulkRepository, times(1))
                .saveAllByBulk(users, folder);
    }
}