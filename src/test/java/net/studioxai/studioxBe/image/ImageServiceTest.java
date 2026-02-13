package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock private ImageRepository imageRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private FolderRepository folderRepository;

    @Mock private S3UrlHandler s3UrlHandler;
    @Mock private S3ImageLoader s3ImageLoader;
    @Mock private S3ImageUploader s3ImageUploader;
    @Mock private GeminiImageClient geminiImageClient;

    @Mock private FolderManagerService folderManagerService;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("Presign URL 발급 성공")
    void issuePresign_success() {

        S3Url s3Url = S3Url.to("uploadUrl", "images/raw/test.png");

        when(s3UrlHandler.handle("images/raw")).thenReturn(s3Url);

        PresignResponse response = imageService.issuePresign();

        assertThat(response.uploadUrl()).isEqualTo("uploadUrl");
        assertThat(response.rawImageObjectKey()).isEqualTo("images/raw/test.png");
    }

    @Test
    @DisplayName("컷아웃 이미지 생성 성공")
    void generateCutoutImage_success() {
        Long userId = 1L;
        Long folderId = 10L;

        CutoutImageGenerateRequest request =
                new CutoutImageGenerateRequest("images/raw/input.png", folderId);

        Folder folder = mock(Folder.class);

        when(folderRepository.findById(folderId))
                .thenReturn(Optional.of(folder));

        doNothing().when(folderManagerService)
                .isUserWritable(userId, folderId);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project p = invocation.getArgument(0);
                    ReflectionTestUtils.setField(p, "id", 100L);
                    return p;
                });

        String raw = Base64.getEncoder().encodeToString("raw".getBytes());
        String cutout = Base64.getEncoder().encodeToString("cutout".getBytes());

        when(s3ImageLoader.loadAsBase64(anyString())).thenReturn(raw);
        when(geminiImageClient.removeBackground(anyString())).thenReturn(cutout);

        CutoutImageGenerateResponse response =
                imageService.generateCutoutImage(userId, request);

        assertThat(response.projectId()).isEqualTo(100L);
        assertThat(response.cutoutImageObjectKey())
                .contains("images/100/cutout/");

        verify(s3ImageUploader, times(1))
                .upload(anyString(), any());
        }

    @Test
    @DisplayName("합성 이미지 생성 성공")
    void generateImage_success() {

        // given
        Long userId = 1L;
        Long projectId = 100L;
        Long templateId = 20L;

        // --- Folder mock ---
        Folder folder = mock(Folder.class);
        when(folder.getId()).thenReturn(10L);

        // --- Project mock ---
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(project.getFolder()).thenReturn(folder);

        // --- Template mock ---
        Template template = mock(Template.class);
        when(template.getImageObjectKey())
                .thenReturn("templates/template.png");

        // --- Repository stubbing ---
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(templateRepository.findById(templateId))
                .thenReturn(Optional.of(template));

        // --- 권한 검증 ---
        doNothing().when(folderManagerService)
                .isUserWritable(eq(userId), eq(10L));

        // --- Base64 데이터 (디코딩 가능한 값) ---
        String encoded = Base64.getEncoder()
                .encodeToString("test".getBytes());

        when(s3ImageLoader.loadAsBase64(anyString()))
                .thenReturn(encoded);

        when(geminiImageClient.generateCompositeImage(anyString(), anyString()))
                .thenReturn(encoded);

        when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ImageGenerateRequest request =
                new ImageGenerateRequest(
                        "images/100/cutout/test.png",
                        templateId,
                        projectId
                );

        // when
        ImageGenerateResponse response =
                imageService.generateImage(userId, request);

        // then
        assertThat(response).isNotNull();

        // 업로드 검증
        verify(s3ImageUploader, times(1))
                .upload(contains("images/100/result/"), any());

        // 도메인 상태 변경 검증
        verify(project, times(1)).updateTemplate(template);
        verify(project, times(1)).updateRepresentativeImage(anyString());
    }



    @Test
    @DisplayName("이미지 상세 조회 성공")
    void getImage_success() {

        Template template = mock(Template.class);
        when(template.getId()).thenReturn(10L);

        Project project = mock(Project.class);
        when(project.getTemplate()).thenReturn(template);

        Image image = mock(Image.class);
        when(image.getProject()).thenReturn(project);

        when(imageRepository.findDetailById(1L))
                .thenReturn(Optional.of(image));

        ImageDetailResponse response = imageService.getImage(1L);

        assertThat(response).isNotNull();
    }
}
