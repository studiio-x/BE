package net.studioxai.studioxBe.folder;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.RootFolderDto;
import net.studioxai.studioxBe.domain.folder.dto.projection.RootFolderProjection;
import net.studioxai.studioxBe.domain.folder.dto.request.FolderCreateRequest;
import net.studioxai.studioxBe.domain.folder.dto.response.FoldersResponse;
import net.studioxai.studioxBe.domain.folder.dto.response.MyFolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.entity.enums.LinkMode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.*;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.folder.service.FolderService;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock FolderRepository folderRepository;
    @Mock
    FolderManagerService folderManagerService;
    @Mock ClosureFolderInsertRepository closureFolderInsertRepository;
    @Mock ClosureFolderRepository closureFolderRepository;
    @Mock FolderManagerBulkRepository folderManagerBulkRepository;
    @Mock FolderManagerRepository folderManagerRepository;
    @Mock ClosureFolderMoveRepository closureFolderMoveRepository;

    private FolderService sut() {
        return new FolderService(
                folderRepository,
                folderManagerService,
                closureFolderInsertRepository,
                closureFolderRepository,
                folderManagerBulkRepository,
                folderManagerRepository,
                closureFolderMoveRepository
        );
    }

    @Test
    @DisplayName("changeLinkMode: 링크가 해제된 상태(=isLink false)라면 매니저 upsert + 서브트리 ACL root 갱신")
    void changeLinkMode_whenBecomesNotLink_thenUpsertAndUpdateAclRoot() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long parentId = 99L;

        Folder folder = mock(Folder.class);
        Folder parent = mock(Folder.class);

        when(folderRepository.findById(folderId)).thenReturn(java.util.Optional.of(folder));
        when(folder.getParentFolder()).thenReturn(parent);
        when(parent.getId()).thenReturn(parentId);

        when(folder.getLinkMode()).thenReturn(LinkMode.UNLINK);

        List<FolderManagerDto> managers = List.of(mock(FolderManagerDto.class), mock(FolderManagerDto.class));
        when(folderManagerService.getManagers(parentId)).thenReturn(managers);

        // when
        sut().changeLinkMode(userId, folderId);

        // then
        InOrder inOrder = inOrder(folderManagerService, folderRepository, folder, folderManagerService, folderManagerBulkRepository);

        inOrder.verify(folderManagerService).isUserWritable(userId, folderId);
        inOrder.verify(folderRepository).findById(folderId);
        inOrder.verify(folder).updateLinkMode();
        inOrder.verify(folderRepository).flush();
        inOrder.verify(folderManagerService).getManagers(parentId);

        verify(folderManagerBulkRepository).upsertManagersForFolder(folderId, managers);
        verify(folderRepository).updateAclRootForSubtree(10L, folderId);

        verify(folderManagerBulkRepository, never()).deleteManagersForFolder(anyLong(), anyList());
        verify(folderRepository, never()).updateAclRootForSubtreeToParentAclRoot(anyLong());
    }

    @Test
    @DisplayName("changeLinkMode: 링크 상태(=isLink true)라면 매니저 삭제 + 서브트리 ACL root를 부모 ACL root로 갱신")
    void changeLinkMode_whenBecomesLink_thenDeleteAndUpdateAclRootToParent() {
        // given
        Long userId = 1L;
        Long folderId = 10L;
        Long parentId = 99L;

        Folder folder = mock(Folder.class);
        Folder parent = mock(Folder.class);

        when(folderRepository.findById(folderId)).thenReturn(java.util.Optional.of(folder));
        when(folder.getParentFolder()).thenReturn(parent);
        when(parent.getId()).thenReturn(parentId);

        when(folder.getLinkMode()).thenReturn(LinkMode.LINK);

        List<FolderManagerDto> managers = List.of(mock(FolderManagerDto.class));
        when(folderManagerService.getManagers(parentId)).thenReturn(managers);

        // when
        sut().changeLinkMode(userId, folderId);

        // then
        verify(folderManagerService).isUserWritable(userId, folderId);
        verify(folder).updateLinkMode();
        verify(folderRepository).flush();
        verify(folderManagerService).getManagers(parentId);

        verify(folderManagerBulkRepository).deleteManagersForFolder(folderId, managers);
        verify(folderRepository).updateAclRootForSubtreeToParentAclRoot(folderId);

        verify(folderManagerBulkRepository, never()).upsertManagersForFolder(anyLong(), anyList());
        verify(folderRepository, never()).updateAclRootForSubtree(anyLong(), eq(10L));
    }

    @Test
    @DisplayName("changeLinkMode: 폴더가 없으면 예외")
    void changeLinkMode_whenFolderNotFound_thenThrow() {
        // given
        Long userId = 1L;
        Long folderId = 404L;

        when(folderRepository.findById(folderId)).thenReturn(java.util.Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().changeLinkMode(userId, folderId))
                .isInstanceOf(FolderExceptionHandler.class);

        verify(folderManagerService).isUserWritable(userId, folderId);
        verify(folderRepository).findById(folderId);
        verifyNoMoreInteractions(folderRepository, folderManagerBulkRepository, closureFolderRepository, closureFolderInsertRepository);
    }

    @Test
    @DisplayName("createRootFolder: 루트 폴더 생성/저장 + 루트 매니저 생성 + 클로저 삽입 + ACL 루트 ID 업데이트")
    void createRootFolder_success() {
        // given
        String folderName = "root";
        User user = mock(User.class);

        Folder rootFolder = mock(Folder.class);
        Long rootId = 123L;
        when(rootFolder.getId()).thenReturn(rootId);

        try (MockedStatic<Folder> mocked = mockStatic(Folder.class)) {
            mocked.when(() -> Folder.createRoot(folderName)).thenReturn(rootFolder);

            // when
            Folder result = sut().createRootFolder(folderName, user);

            // then
            assertThat(result).isSameAs(rootFolder);

            mocked.verify(() -> Folder.createRoot(folderName));
            verify(folderRepository).saveAndFlush(rootFolder);
            verify(folderManagerService).createRootManager(user, rootFolder);
            verify(closureFolderInsertRepository).insertClosureForNewFolder(null, rootId);
            verify(rootFolder).updateRootAclId();
        }
    }

    @Test
    @DisplayName("createSubFolder: 부모 폴더 조회 + 권한 확인 + 서브 폴더 생성/저장 + 클로저 삽입")
    void createSubFolder_success() {
        // given
        Long userId = 1L;
        Long parentFolderId = 10L;

        FolderCreateRequest req = mock(FolderCreateRequest.class);
        when(req.name()).thenReturn("sub");

        Folder parent = mock(Folder.class);
        when(parent.getId()).thenReturn(parentFolderId);
        when(folderRepository.findById(parentFolderId)).thenReturn(java.util.Optional.of(parent));

        Folder sub = mock(Folder.class);
        Long subId = 77L;
        when(sub.getId()).thenReturn(subId);

        try (MockedStatic<Folder> mocked = mockStatic(Folder.class)) {
            mocked.when(() -> Folder.createSub("sub", parent)).thenReturn(sub);

            // when
            sut().createSubFolder(userId, parentFolderId, req);

            // then
            verify(folderManagerService).isUserWritable(userId, parentFolderId);
            verify(folderRepository).saveAndFlush(sub);
            verify(closureFolderInsertRepository).insertClosureForNewFolder(parentFolderId, subId);
            mocked.verify(() -> Folder.createSub("sub", parent));
        }
    }

    @Test
    @DisplayName("createSubFolder: 부모 폴더가 없으면 예외")
    void createSubFolder_whenParentNotFound_thenThrow() {
        // given
        Long userId = 1L;
        Long parentFolderId = 999L;

        FolderCreateRequest req = mock(FolderCreateRequest.class);
        when(folderRepository.findById(parentFolderId)).thenReturn(java.util.Optional.empty());

        // when + then
        assertThatThrownBy(() -> sut().createSubFolder(userId, parentFolderId, req))
                .isInstanceOf(FolderExceptionHandler.class);

        verify(folderRepository).findById(parentFolderId);
        verify(folderManagerService, never()).isUserWritable(anyLong(), anyLong());
        verify(folderRepository, never()).saveAndFlush(any(Folder.class));
        verify(closureFolderInsertRepository, never()).insertClosureForNewFolder(any(), any());
    }

    @Test
    @DisplayName("findFolders: isOwner=1은 myProject, isOwner=0은 sharedProjects로 분리")
    void findMyFoldes_success() {
        // given
        Long userId = 1L;

        List<RootFolderProjection> rows = List.of(
                new TestRootFolderProjection(10L, "A", 1),
                new TestRootFolderProjection(20L, "B", 0),
                new TestRootFolderProjection(30L, "C", 1)
        );
        when(closureFolderRepository.findMyFolders(userId)).thenReturn(rows);

        // when
        MyFolderResponse res = sut().findMyFoldes(userId);

        // then
        assertThat(res).isNotNull();

        List<RootFolderDto> myProject = res.myProject();
        List<RootFolderDto> sharedProjects = res.sharedProject();

        assertThat(myProject).extracting(RootFolderDto::folderId).containsExactlyInAnyOrder(10L, 30L);
        assertThat(sharedProjects).extracting(RootFolderDto::folderId).containsExactly(20L);
    }

    static class TestRootFolderProjection implements RootFolderProjection {
        private final Long folderId;
        private final String name;
        private final int isOwner;

        TestRootFolderProjection(Long folderId, String name, int isOwner) {
            this.folderId = folderId;
            this.name = name;
            this.isOwner = isOwner;
        }

        @Override public Long getFolderId() { return folderId; }
        @Override public String getName() { return name; }
        @Override public Integer getIsOwner() { return isOwner; }
    }

    @Test
    void findFoldersByFolderId_folderNotFound_throw() {
        // given
        Long userId = 1L;
        Long folderId = 10L;

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                sut().findFoldersByFolderId(userId, folderId, Sort.Direction.DESC, 0, 10)
        ).isInstanceOf(FolderExceptionHandler.class);

        verify(folderManagerService, never()).canVisited(anyLong(), anyLong(), any());
        verify(folderRepository, never()).findByParentFolder(any(), any(Pageable.class));
    }

    @Test
    void findFoldersByFolderId_success_returnsDtosAndPageInfo() {
        // given
        Long userId = 1L;
        Long folderId = 10L;

        Folder parent = mock(Folder.class);
        when(parent.getAclRootFolderId()).thenReturn(999L);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(parent));

        Folder child1 = mock(Folder.class);
        when(child1.getId()).thenReturn(101L);
        when(child1.getName()).thenReturn("child1");

        Folder child2 = mock(Folder.class);
        when(child2.getId()).thenReturn(102L);
        when(child2.getName()).thenReturn("child2");

        int pageNum = 0;
        int limit = 2;
        Sort.Direction sort = Sort.Direction.DESC;

        PageRequest expectedPageRequest = PageRequest.of(pageNum, limit, Sort.by(sort, "createdAt"));
        Page<Folder> page = new PageImpl<>(
                List.of(child1, child2),
                expectedPageRequest,
                5 // total elements
        );

        when(folderRepository.findByParentFolder(eq(parent), any(Pageable.class)))
                .thenReturn(page);

        // when
        FoldersResponse res = sut().findFoldersByFolderId(userId, folderId, sort, pageNum, limit);

        // then
        verify(folderManagerService).canVisited(userId, folderId, 999L);
        verify(folderRepository).findByParentFolder(eq(parent), any(Pageable.class));

        assertThat(res).isNotNull();
        assertThat(res.folders()).hasSize(2);
        assertThat(res.folders().get(0).folderId()).isEqualTo(101L);
        assertThat(res.folders().get(0).folderName()).isEqualTo("child1");
        assertThat(res.folders().get(1).folderId()).isEqualTo(102L);
        assertThat(res.folders().get(1).folderName()).isEqualTo("child2");

        assertThat(res.pageInfo().getPageNum()).isEqualTo(pageNum);
        assertThat(res.pageInfo().getLimit()).isEqualTo(limit);
        assertThat(res.pageInfo().getTotalElements()).isEqualTo(5);
        assertThat(res.pageInfo().getTotalPages()).isEqualTo(page.getTotalPages());
    }

    @Test
    void deleteFolder_success_deletesManagersClosuresFoldersInOrder() {
        // given
        Long userId = 1L;
        Long folderId = 5L;

        Folder folder = mock(Folder.class);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        List<Long> subFolderIds = List.of(5L, 6L, 7L);
        when(closureFolderRepository.findDescendantFolderIds(folderId)).thenReturn(subFolderIds);

        // when
        sut().deleteFolder(userId, folderId);

        // then
        InOrder inOrder = inOrder(folderManagerService, folderRepository, closureFolderRepository, folderManagerRepository);

        inOrder.verify(folderManagerService).isUserWritable(userId, folderId);
        inOrder.verify(folderRepository).findById(folderId);

        inOrder.verify(folderManagerRepository).deleteAllByFolderIds(subFolderIds);

        inOrder.verify(closureFolderRepository).deleteByAncestorIds(subFolderIds);
        inOrder.verify(closureFolderRepository).deleteByDescendantIds(subFolderIds);

        // for-loop delete + flush
        inOrder.verify(folderRepository).deleteById(5L);
        inOrder.verify(folderRepository).flush();
        inOrder.verify(folderRepository).deleteById(6L);
        inOrder.verify(folderRepository).flush();
        inOrder.verify(folderRepository).deleteById(7L);
        inOrder.verify(folderRepository).flush();

        verifyNoMoreInteractions(folderRepository);
    }

    @Test
    void deleteFolder_folderNotFound_throw() {
        // given
        Long userId = 1L;
        Long folderId = 999L;

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut().deleteFolder(userId, folderId))
                .isInstanceOf(FolderExceptionHandler.class);

        verify(folderManagerService).isUserWritable(userId, folderId);
        verify(closureFolderRepository, never()).findDescendantFolderIds(anyLong());
        verify(folderManagerRepository, never()).deleteAllByFolderIds(anyList());
    }

    @Test
    void updateFolderName_success_callsUpdateName() {
        // given
        Long userId = 1L;
        Long folderId = 10L;

        Folder folder = mock(Folder.class);
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        FolderCreateRequest req = mock(FolderCreateRequest.class);
        when(req.name()).thenReturn("new-name");

        // when
        sut().updateFolderName(userId, folderId, req);

        // then
        verify(folderManagerService).isUserWritable(userId, folderId);
        verify(folderRepository).findById(folderId);
        verify(folder).updateName("new-name");
    }

    @Test
    void moveFolder_success_movesAndUpdatesClosureAndAclRoot() {
        // given
        Long userId = 1L;
        Long targetFolderId = 10L;
        Long destinationFolderId = 20L;

        Folder target = mock(Folder.class);
        Folder destination = mock(Folder.class);

        when(folderRepository.findById(targetFolderId)).thenReturn(Optional.of(target));
        when(folderRepository.findById(destinationFolderId)).thenReturn(Optional.of(destination));

        when(closureFolderMoveRepository.existsPath(targetFolderId, destinationFolderId)).thenReturn(false);

        when(destination.getAclRootFolderId()).thenReturn(777L);

        // when
        sut().moveFolder(userId, targetFolderId, destinationFolderId);

        // then
        verify(folderManagerService).isUserWritable(userId, targetFolderId);
        verify(folderManagerService).isUserWritable(userId, destinationFolderId);

        verify(target).move(destination);

        verify(closureFolderMoveRepository).existsPath(targetFolderId, destinationFolderId);
        verify(closureFolderMoveRepository).deleteOldAncestorLinks(targetFolderId);
        verify(closureFolderMoveRepository).insertNewAncestorLinks(targetFolderId, destinationFolderId);

        verify(folderRepository).updateAclRootForSubtree(targetFolderId, 777L);
    }

    @Test
    void moveFolder_whenDestinationIsDescendant_throwInvalidHierarchy() {
        // given
        Long userId = 1L;
        Long targetFolderId = 10L;
        Long destinationFolderId = 20L;

        Folder target = mock(Folder.class);
        Folder destination = mock(Folder.class);

        when(folderRepository.findById(targetFolderId)).thenReturn(Optional.of(target));
        when(folderRepository.findById(destinationFolderId)).thenReturn(Optional.of(destination));

        when(closureFolderMoveRepository.existsPath(targetFolderId, destinationFolderId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> sut().moveFolder(userId, targetFolderId, destinationFolderId))
                .isInstanceOf(FolderExceptionHandler.class);

        verify(closureFolderMoveRepository, never()).deleteOldAncestorLinks(anyLong());
        verify(closureFolderMoveRepository, never()).insertNewAncestorLinks(anyLong(), anyLong());
        verify(folderRepository, never()).updateAclRootForSubtree(anyLong(), anyLong());
        verify(target, never()).move(any());
    }

    @Test
    void moveFolder_whenMoveToItself_throw() {
        // given
        Long userId = 1L;
        Long sameId = 10L;

        Folder target = mock(Folder.class);
        when(folderRepository.findById(sameId)).thenReturn(Optional.of(target));

        when(closureFolderMoveRepository.existsPath(sameId, sameId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> sut().moveFolder(userId, sameId, sameId))
                .isInstanceOf(FolderExceptionHandler.class);

        verify(closureFolderMoveRepository, never()).deleteOldAncestorLinks(anyLong());
        verify(closureFolderMoveRepository, never()).insertNewAncestorLinks(anyLong(), anyLong());
        verify(folderRepository, never()).updateAclRootForSubtree(anyLong(), anyLong());
    }
}
