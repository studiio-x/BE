package net.studioxai.studioxBe.project;

import net.studioxai.studioxBe.domain.project.dto.ProjectCreateRequest;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.project.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.project.service.ProjectManagerService;
import net.studioxai.studioxBe.domain.project.service.ProjectService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectManagerService projectManagerService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("createProject: 유저 ID와 요청 DTO를 받아 프로젝트를 생성하고 매니저를 추가한다")
    void createProject_success() {
        // given
        Long userId = 1L;
        ProjectCreateRequest request = new ProjectCreateRequest("테스트 프로젝트");
        User user = mock(User.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);

        // when
        projectService.createProject(userId, request);

        // then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getName()).isEqualTo("테스트 프로젝트");

        verify(projectManagerService).addManager(savedProject, user, true);

        verify(userService, times(1)).getUserByIdOrThrow(userId);
    }

    @Test
    @DisplayName("addProject: User와 이름, isAdmin으로 프로젝트 생성 후 저장 및 매니저 등록")
    void addProject_success() {
        // given
        User user = mock(User.class);
        String name = "다른 테스트 프로젝트";
        boolean isAdmin = true;

        // when
        projectService.addProject(user, name, isAdmin);

        // then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getName()).isEqualTo(name);

        verify(projectManagerService).addManager(savedProject, user, isAdmin);
    }
}
