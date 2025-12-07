package net.studioxai.studioxBe.project;

import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerErrorCode;
import net.studioxai.studioxBe.domain.project.exception.ProjectMangerExceptionHandler;
import net.studioxai.studioxBe.domain.project.repository.ProjectManagerRepository;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.user.entity.User;
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
}