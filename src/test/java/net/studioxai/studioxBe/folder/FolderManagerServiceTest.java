package net.studioxai.studioxBe.folder;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.PermissionDto;
import net.studioxai.studioxBe.domain.folder.dto.projection.FolderManagerProjection;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderManagerAddRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FolderManagersResponse;
import net.studioxai.studioxBe.domain.folder.dto.RootFolderDto;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.entity.enums.LinkMode;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.ClosureFolderRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderManagerRepository;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderManagerServiceTest {

    @Mock FolderManagerRepository folderManagerRepository;
    @Mock UserService userService;
    @Mock FolderRepository folderRepository;
    @Mock ClosureFolderRepository closureFolderRepository;

    private FolderManagerService sut() {
        return new FolderManagerService(
                folderManagerRepository,
                userService,
                folderRepository,
                closureFolderRepository
        );
    }

    @Test
    @DisplayName("getManagers(userId, folderId): 매니저 목록이 비었으면 예외")
    void getManagersResponse_whenManagersEmpty_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findAllUserPermissions(folderId, aclRootId)).thenReturn(List.of());

        // when + then
        assertThatThrownBy(() -> sut().getManagers(userId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);
    }

    @Test
    @DisplayName("getManagers(userId, folderId): 내 권한이 없으면 예외")
    void getManagersResponse_whenMeNotFound_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        var p1 = mockPermissionRow(2L, Permission.WRITE);
        when(closureFolderRepository.findAllUserPermissions(folderId, aclRootId)).thenReturn(List.of(p1));

        // when + then
        assertThatThrownBy(() -> sut().getManagers(userId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);
    }

    @Test
    @DisplayName("getManagers(userId, folderId): 내 권한 포함해서 응답 생성")
    void getManagersResponse_success() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        var meRow = mockPermissionRow(userId, Permission.OWNER);
        var otherRow = mockPermissionRow(2L, Permission.WRITE);
        when(closureFolderRepository.findAllUserPermissions(folderId, aclRootId)).thenReturn(List.of(meRow, otherRow));

        // when
        FolderManagersResponse res = sut().getManagers(userId, folderId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.myPermission()).isEqualTo(PermissionDto.create(Permission.OWNER));
        assertThat(res.managers()).hasSize(2);
        assertThat(res.managers()).extracting(FolderManagerDto::userId).containsExactlyInAnyOrder(userId, 2L);
    }

    @Test
    @DisplayName("updatePermission: actor가 writable 아니면 예외(권한 없음)")
    void updatePermission_whenActorNotWritable_thenThrow() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folderForPerm = mock(Folder.class);
        when(folderForPerm.getAclRootFolderId()).thenReturn(aclRootId);

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folderForPerm));

        when(closureFolderRepository.findPermission(folderId, aclRootId, actorUserId))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().updatePermission(actorUserId, targetUserId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(folderManagerRepository, never()).findByFolderIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("updatePermission: 기존 FolderManager가 있으면 updateDirectPermission만 호출")
    void updatePermission_whenManagerExists_thenUpdateDirectPermission() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folderForPerm = mock(Folder.class);
        when(folderForPerm.getAclRootFolderId()).thenReturn(aclRootId);

        Folder folderEntity = mock(Folder.class);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folderForPerm))  // actor perm
                .thenReturn(Optional.of(folderForPerm))  // target perm
                .thenReturn(Optional.of(folderEntity));  // real folder load

        when(closureFolderRepository.findPermission(folderId, aclRootId, actorUserId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.findPermission(folderId, aclRootId, targetUserId))
                .thenReturn(Optional.of(Permission.READ));

        FolderManager existing = mock(FolderManager.class);
        when(folderManagerRepository.findByFolderIdAndUserId(folderId, targetUserId))
                .thenReturn(Optional.of(existing));

        // when
        sut().updatePermission(actorUserId, targetUserId, folderId);

        // then
        verify(folderManagerRepository).findByFolderIdAndUserId(folderId, targetUserId);
        verify(existing).updateDirectPermission();
        verify(folderManagerRepository, never()).save(any());
        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("updatePermission: 매니저가 없고 폴더가 UNLINK가 아니면 예외")
    void updatePermission_whenManagerMissingAndNotUnlink_thenThrow() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folderForPerm = mock(Folder.class);
        when(folderForPerm.getAclRootFolderId()).thenReturn(aclRootId);

        Folder folderEntity = mock(Folder.class);
        when(folderEntity.getLinkMode()).thenReturn(LinkMode.LINK);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folderForPerm))  // actor perm
                .thenReturn(Optional.of(folderForPerm))  // target perm
                .thenReturn(Optional.of(folderEntity));  // real folder load

        when(closureFolderRepository.findPermission(folderId, aclRootId, actorUserId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.findPermission(folderId, aclRootId, targetUserId))
                .thenReturn(Optional.of(Permission.READ));

        when(folderManagerRepository.findByFolderIdAndUserId(folderId, targetUserId))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().updatePermission(actorUserId, targetUserId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(folderManagerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePermission: 매니저가 없고 UNLINK면 생성 후 updateDirectPermission 호출")
    void updatePermission_whenManagerMissingAndUnlink_thenCreateAndUpdate() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folderForPerm = mock(Folder.class);
        when(folderForPerm.getAclRootFolderId()).thenReturn(aclRootId);

        Folder folderEntity = mock(Folder.class);
        when(folderEntity.getLinkMode()).thenReturn(LinkMode.UNLINK);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folderForPerm))  // actor perm
                .thenReturn(Optional.of(folderForPerm))  // target perm
                .thenReturn(Optional.of(folderEntity));  // real folder load

        when(closureFolderRepository.findPermission(folderId, aclRootId, actorUserId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.findPermission(folderId, aclRootId, targetUserId))
                .thenReturn(Optional.of(Permission.READ));

        when(folderManagerRepository.findByFolderIdAndUserId(folderId, targetUserId))
                .thenReturn(Optional.empty());

        User targetUser = mock(User.class);
        when(userService.getUserByIdOrThrow(targetUserId)).thenReturn(targetUser);

        FolderManager saved = mock(FolderManager.class);

        try (MockedStatic<FolderManager> mocked = mockStatic(FolderManager.class)) {
            mocked.when(() -> FolderManager.create(targetUser, folderEntity, Permission.READ))
                    .thenReturn(saved);

            when(folderManagerRepository.save(saved)).thenReturn(saved);

            // when
            sut().updatePermission(actorUserId, targetUserId, folderId);

            // then
            mocked.verify(() -> FolderManager.create(targetUser, folderEntity, Permission.READ));
            verify(folderManagerRepository).save(saved);
            verify(saved).updateDirectPermission();
        }
    }

    @Test
    @DisplayName("inviteManager: 폴더 없으면 예외")
    void inviteManager_whenFolderNotFound_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        FolderManagerAddRequest req = mock(FolderManagerAddRequest.class);

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().inviteManager(userId, folderId, req))
                .isInstanceOf(FolderManagerExceptionHandler.class);
    }

    @Test
    @DisplayName("inviteManager: writable 아니면 예외(권한 없음)")
    void inviteManager_whenNotWritable_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder)) // inviteManager에서 폴더 조회
                .thenReturn(Optional.of(folder)); // isUserWritable 내부 getPermission에서 다시 조회

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.empty());

        FolderManagerAddRequest req = mock(FolderManagerAddRequest.class);

        // when + then
        assertThatThrownBy(() -> sut().inviteManager(userId, folderId, req))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(closureFolderRepository, never()).countManagers(anyLong(), anyLong());
        verify(userService, never()).getUserByEmailOrThrow(anyString());
    }

    @Test
    @DisplayName("inviteManager: 매니저 수 제한 초과면 예외")
    void inviteManager_whenLimitExceeded_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder))
                .thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.countManagers(folderId, aclRootId)).thenReturn(5L);

        FolderManagerAddRequest req = mock(FolderManagerAddRequest.class);

        // when + then
        assertThatThrownBy(() -> sut().inviteManager(userId, folderId, req))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(userService, never()).getUserByEmailOrThrow(anyString());
    }

    @Test
    @DisplayName("inviteManager: 이미 권한 있으면 예외")
    void inviteManager_whenAlreadyManager_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder))
                .thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.countManagers(folderId, aclRootId)).thenReturn(4L);

        FolderManagerAddRequest req = mock(FolderManagerAddRequest.class);
        when(req.email()).thenReturn("test@test.com");

        User invited = mock(User.class);
        when(invited.getId()).thenReturn(2L);
        when(userService.getUserByEmailOrThrow("test@test.com")).thenReturn(invited);

        when(closureFolderRepository.findPermission(folderId, aclRootId, 2L))
                .thenReturn(Optional.of(Permission.READ));

        // when + then
        assertThatThrownBy(() -> sut().inviteManager(userId, folderId, req))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(folderManagerRepository, never()).save(any());
    }

    @Test
    @DisplayName("inviteManager: 정상 초대면 createWritableManager 흐름 수행")
    void inviteManager_success() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder))
                .thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.WRITE));
        when(closureFolderRepository.countManagers(folderId, aclRootId)).thenReturn(4L);

        FolderManagerAddRequest req = mock(FolderManagerAddRequest.class);
        when(req.email()).thenReturn("test@test.com");

        User invited = mock(User.class);
        when(invited.getId()).thenReturn(2L);
        when(userService.getUserByEmailOrThrow("test@test.com")).thenReturn(invited);

        when(closureFolderRepository.findPermission(folderId, aclRootId, 2L))
                .thenReturn(Optional.empty());

        User invitedLoaded = mock(User.class);
        when(userService.getUserByIdOrThrow(2L)).thenReturn(invitedLoaded);

        FolderManager writerManager = mock(FolderManager.class);

        try (MockedStatic<FolderManager> mocked = mockStatic(FolderManager.class)) {
            mocked.when(() -> FolderManager.createWriter(invitedLoaded, folder)).thenReturn(writerManager);

            // when
            sut().inviteManager(userId, folderId, req);

            // then
            mocked.verify(() -> FolderManager.createWriter(invitedLoaded, folder));
            verify(folderManagerRepository).save(writerManager);
        }
    }

    @Test
    @DisplayName("createRootManager: 루트 매니저 생성 후 저장")
    void createRootManager_success() {
        // given
        User user = mock(User.class);
        Folder folder = mock(Folder.class);
        FolderManager rootManager = mock(FolderManager.class);

        try (MockedStatic<FolderManager> mocked = mockStatic(FolderManager.class)) {
            mocked.when(() -> FolderManager.createRootManager(user, folder)).thenReturn(rootManager);

            // when
            sut().createRootManager(user, folder);

            // then
            mocked.verify(() -> FolderManager.createRootManager(user, folder));
            verify(folderManagerRepository).save(rootManager);
        }
    }

    @Test
    @DisplayName("createWritableManager: 유저 조회 후 writer 매니저 생성/저장")
    void createWritableManager_success() {
        // given
        Long userId = 1L;
        Folder folder = mock(Folder.class);

        User user = mock(User.class);
        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);

        FolderManager writerManager = mock(FolderManager.class);

        try (MockedStatic<FolderManager> mocked = mockStatic(FolderManager.class)) {
            mocked.when(() -> FolderManager.createWriter(user, folder)).thenReturn(writerManager);

            // when
            sut().createWritableManager(userId, folder);

            // then
            mocked.verify(() -> FolderManager.createWriter(user, folder));
            verify(folderManagerRepository).save(writerManager);
        }
    }

    @Test
    @DisplayName("getManagers(folderId): 폴더 없으면 예외")
    void getManagers_whenFolderNotFound_thenThrow() {
        // given
        Long folderId = 10L;
        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().getManagers(folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(closureFolderRepository, never()).findAllUserPermissions(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getFolders: repo 그대로 반환")
    void getFolders_success() {
        // given
        Long userId = 1L;
        List<RootFolderDto> rows = List.of(mock(RootFolderDto.class), mock(RootFolderDto.class));
        when(folderManagerRepository.findByUserId(userId)).thenReturn(rows);

        // when
        List<RootFolderDto> res = sut().getFolders(userId);

        // then
        assertThat(res).isSameAs(rows);
    }

    @Test
    @DisplayName("isUserWritable: 권한 레코드 없으면 예외")
    void isUserWritable_whenPermissionMissing_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().isUserWritable(userId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);
    }

    @Test
    @DisplayName("isUserWritable: 통과 케이스")
    void isUserWritable_success() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.WRITE));

        // when
        sut().isUserWritable(userId, folderId);

        // then
        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
    }

    @Test
    @DisplayName("isUserAdmin: readable(WRITE/OWNER) 이면 통과")
    void isUserReadable_success() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.WRITE));

        // when
        sut().isUserReadable(userId, folderId);

        // then
        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
    }

    private static FolderManagerProjection mockPermissionRow(
            Long userId,
            Permission permission
    ) {
        FolderManagerProjection p = mock(FolderManagerProjection.class);

        when(p.getUserId()).thenReturn(userId);
        when(p.getProfileUrl()).thenReturn("p");
        when(p.getUsername()).thenReturn("u" + userId);
        when(p.getEmail()).thenReturn("e" + userId + "@t.com");
        when(p.getPermission()).thenReturn(permission);

        return p;
    }

    @Test
    @DisplayName("canVisited: 현재 폴더에서 읽기 가능하면 통과")
    void canVisited_whenCanReadHere_thenPass() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.READ));
        when(closureFolderRepository.existsReadableDescendant(folderId, aclRootId, userId))
                .thenReturn(0L);

        // when + then (no exception)
        assertThatCode(() -> sut().canVisited(userId, folderId, aclRootId))
                .doesNotThrowAnyException();

        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
        verify(closureFolderRepository).existsReadableDescendant(folderId, aclRootId, userId);
    }

    @Test
    @DisplayName("canVisited: 현재 폴더에서 읽기 불가 + 하위 readable descendant 없으면 예외")
    void canVisited_whenCannotReadAndNoReadableDescendant_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.empty()); // canReadHere = false
        when(closureFolderRepository.existsReadableDescendant(folderId, aclRootId, userId))
                .thenReturn(0L);

        // when + then
        assertThatThrownBy(() -> sut().canVisited(userId, folderId, aclRootId))
                .isInstanceOf(FolderManagerExceptionHandler.class);

        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
        verify(closureFolderRepository).existsReadableDescendant(folderId, aclRootId, userId);
    }

    @Test
    @DisplayName("canVisited: 현재 폴더에서 읽기 불가여도 하위 readable descendant 있으면 통과")
    void canVisited_whenCannotReadButHasReadableDescendant_thenPass() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.empty()); // canReadHere = false
        when(closureFolderRepository.existsReadableDescendant(folderId, aclRootId, userId))
                .thenReturn(1L); // canTraverse > 0

        // when + then
        assertThatCode(() -> sut().canVisited(userId, folderId, aclRootId))
                .doesNotThrowAnyException();

        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
        verify(closureFolderRepository).existsReadableDescendant(folderId, aclRootId, userId);
    }

    @Test
    @DisplayName("isUserReadable: 권한 레코드 없으면 예외")
    void isUserReadable_whenPermissionMissing_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().isUserReadable(userId, folderId))
                .isInstanceOf(FolderManagerExceptionHandler.class);
    }

    @Test
    @DisplayName("isUserReadable: READ 권한이면 통과")
    void isUserReadable_whenRead_thenPass() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long aclRootId = 100L;

        Folder folder = mock(Folder.class);
        when(folder.getAclRootFolderId()).thenReturn(aclRootId);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        when(closureFolderRepository.findPermission(folderId, aclRootId, userId))
                .thenReturn(Optional.of(Permission.READ));

        // when + then
        assertThatCode(() -> sut().isUserReadable(userId, folderId))
                .doesNotThrowAnyException();

        verify(closureFolderRepository).findPermission(folderId, aclRootId, userId);
    }
}
