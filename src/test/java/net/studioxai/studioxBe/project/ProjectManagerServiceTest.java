package net.studioxai.studioxBe.project;

import net.studioxai.studioxBe.domain.project.dto.ProjectUserResponse;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerErrorCode;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerExceptionHandler;
import net.studioxai.studioxBe.domain.project.repository.ProjectManagerRepository;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectManagerServiceTest {

    @Mock
    private ProjectManagerRepository projectManagerRepository;

    @InjectMocks
    private ProjectManagerService projectManagerService;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("[existProjectMangersOrThrow] 사용자가 프로젝트 매니저 목록에 있으면 예외 없이 통과한다")
    void existProjectMangersOrThrow_ok() {
        // given
        Long userId = 1L;

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);

        ProjectManager manager = mock(ProjectManager.class);
        given(manager.getUser()).willReturn(user);

        List<ProjectManager> managers = List.of(manager);

        // when & then
        assertThatCode(() ->
                projectManagerService.existProjectManagersOrThrow(userId, managers)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[existProjectMangersOrThrow] 사용자가 프로젝트 매니저 목록에 없으면 USER_NO_PROJECT_AUTHORITY 예외 발생")
    void existProjectMangersOrThrow_notExists() {
        // given
        Long userId = 1L;

        User otherUser = mock(User.class);
        given(otherUser.getId()).willReturn(2L);

        ProjectManager manager = mock(ProjectManager.class);
        given(manager.getUser()).willReturn(otherUser);

        List<ProjectManager> managers = List.of(manager);

        // when & then
        assertThatThrownBy(() ->
                projectManagerService.existProjectManagersOrThrow(userId, managers)
        )
                .isInstanceOf(ProjectMangerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(ProjectMangerErrorCode.USER_NO_PROJECT_AUTHORITY);
    }

    @Test
    @DisplayName("[getProjectMangersOrThrow] 프로젝트 매니저가 존재하면 리스트를 반환한다")
    void getProjectMangersOrThrow_ok() {
        // given
        Long projectId = 10L;
        ProjectManager manager = mock(ProjectManager.class);

        given(projectManagerRepository.findByProjectId(projectId))
                .willReturn(List.of(manager));

        // when
        List<ProjectManager> result = projectManagerService.getProjectManagersOrThrow(projectId);

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(manager);
    }

    @Test
    @DisplayName("[getProjectMangersOrThrow] 프로젝트 매니저가 없으면 PROJECT_NOT_FOUND 예외 발생")
    void getProjectMangersOrThrow_empty() {
        // given
        Long projectId = 10L;
        given(projectManagerRepository.findByProjectId(projectId))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() ->
                projectManagerService.getProjectManagersOrThrow(projectId)
        )
                .isInstanceOf(ProjectMangerExceptionHandler.class)
                .extracting("errorCode")
                .isEqualTo(ProjectMangerErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    @DisplayName("유저가 해당 프로젝트 매니저일 경우 ProjectUserResponse 리스트를 반환한다")
    void getProjectManagerList_success() {
        // given
        Long userId = 1L;
        Long projectId = 10L;

        User user = mock(User.class);

        List<ProjectUserResponse> managerList = List.of(
                new ProjectUserResponse(1L, "managerA", "a@example.com", "imgA"),
                new ProjectUserResponse(2L, "managerB", "b@example.com", "imgB")
        );

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(projectManagerRepository.existsByUserAndProjectId(user, projectId)).thenReturn(true);
        when(projectManagerRepository.findManagersByProjectId(projectId)).thenReturn(managerList);

        // when
        List<ProjectUserResponse> result = projectManagerService.getProjectManagerList(userId, projectId);

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).userId()).isEqualTo(1L);
        assertThat(result.get(0).username()).isEqualTo("managerA");
        assertThat(result.get(0).email()).isEqualTo("a@example.com");
        assertThat(result.get(0).profileImageUrl()).isEqualTo("imgA");

        assertThat(result.get(1).username()).isEqualTo("managerB");

        verify(userService).getUserByIdOrThrow(userId);
        verify(projectManagerRepository).existsByUserAndProjectId(user, projectId);
        verify(projectManagerRepository).findManagersByProjectId(projectId);
    }

    @Test
    @DisplayName("유저가 프로젝트 매니저가 아니면 USER_NO_PROJECT_AUTHORITY 예외가 발생한다")
    void getProjectManagerList_fail_noAuthority() {
        // given
        Long userId = 1L;
        Long projectId = 10L;

        User user = mock(User.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(projectManagerRepository.existsByUserAndProjectId(user, projectId)).thenReturn(false);

        // when
        Throwable thrown = catchThrowable(() ->
                projectManagerService.getProjectManagerList(userId, projectId)
        );

        // then
        assertThat(thrown)
                .isInstanceOf(ProjectMangerExceptionHandler.class);

        ProjectMangerExceptionHandler ex = (ProjectMangerExceptionHandler) thrown;
        assertThat(ex.getErrorCode()).isEqualTo(ProjectMangerErrorCode.USER_NO_PROJECT_AUTHORITY);
    }

    @Test
    @DisplayName("유저 조회에 실패하면 예외가 발생한다")
    void getProjectManagerList_fail_userNotFound() {
        // given
        Long userId = 1L;
        Long projectId = 10L;

        when(userService.getUserByIdOrThrow(userId))
                .thenThrow(new IllegalArgumentException("User not found"));

        // when & then
        assertThatThrownBy(() ->
                projectManagerService.getProjectManagerList(userId, projectId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(projectManagerRepository, never()).existsByUserAndProjectId(any(), any());
        verify(projectManagerRepository, never()).findManagersByProjectId(any());
    }
}