package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.image.dto.request.CutoutImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.project.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ImageServiceTest {

    @Mock private ImageRepository imageRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private S3UrlHandler s3UrlHandler;
    @Mock private S3Client s3Client;
    @Mock private GeminiImageClient geminiImageClient;
    @Mock private S3ImageLoader s3ImageLoader;

    @Spy @InjectMocks private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(imageService, "imageDomain", "https://cdn.test/");
    }

    @Test
    @DisplayName("Presign URL 발급 성공")
    void issuePresign_success() {
        S3Url s3Url = S3Url.to("uploadUrl", "images/raw/test.png");

        when(s3UrlHandler.handle("images/raw")).thenReturn(s3Url);

        PresignResponse response = imageService.issuePresign(1L);

        assertThat(response.uploadUrl()).isEqualTo("uploadUrl");
        assertThat(response.rawImageObjectKey()).isEqualTo("images/raw/test.png");
        assertThat(response.rawImageUrl()).isEqualTo("https://cdn.test/images/raw/test.png");
    }

    @Test
    @DisplayName("컷아웃 이미지 생성 성공")
    void generateCutoutImage_success() {
        // given
        CutoutImageGenerateRequest request =
                new CutoutImageGenerateRequest("images/raw/input.png");

        when(s3ImageLoader.loadAsBase64(any()))
                .thenReturn(Base64.getEncoder().encodeToString("raw".getBytes()));

        when(geminiImageClient.removeBackground(any()))
                .thenReturn(Base64.getEncoder().encodeToString("cutout".getBytes()));

        // when
        CutoutImageGenerateResponse response =
                imageService.generateCutoutImage(1L, request);

        // then
        assertThat(response.cutoutImageUrl()).contains("images/cutout/");
        assertThat(response.cutoutImageObjectKey()).contains("images/cutout/");

        verify(s3Client, times(1))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    @DisplayName("합성 이미지 생성 성공")
    void generateImage_success() {
        // given
        Template template = mock(Template.class);
        when(template.getImageUrl()).thenReturn("https://template.test/img.png");

        when(templateRepository.findById(1L))
                .thenReturn(Optional.of(template));

        when(s3ImageLoader.loadAsBase64(any()))
                .thenReturn(Base64.getEncoder().encodeToString("cutout".getBytes()));

        doReturn(Base64.getEncoder().encodeToString("template".getBytes()))
                .when(imageService)
                .loadUrlAsBase64(any());

        when(geminiImageClient.generateCompositeImage(any(), any(), any()))
                .thenReturn(Base64.getEncoder().encodeToString("result".getBytes()));

        when(projectRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(imageRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ImageGenerateRequest request =
                new ImageGenerateRequest("images/cutout/test.png", 1L);

        // when
        ImageGenerateResponse response =
                imageService.generateImage(1L, request);

        // then
        assertThat(response.imageUrl()).contains("images/result/");

        verify(s3Client, times(1))
                .putObject(
                        any(PutObjectRequest.class),
                        any(software.amazon.awssdk.core.sync.RequestBody.class)
                );
    }

    @Test
    @DisplayName("프로젝트 조회 성공")
    void getProject_success() {
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(10L);

        Project project = Project.create(
                "images/cutout/test.png",
                template,
                null
        );

        when(projectRepository.findWithTemplateAndFolderById(1L))
                .thenReturn(Optional.of(project));

        ProjectDetailResponse response = imageService.getProject(1L);

        assertThat(response).isNotNull();
        assertThat(response.templateId()).isEqualTo(10L);
    }


    @Test
    @DisplayName("이미지 상세 조회 성공")
    void getImage_success() {
        // given
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(10L);

        Project project = Project.create(
                "images/cutout/test.png",
                template,
                null
        );

        Image image = Image.create(
                project,
                "images/result/test.png"
        );

        when(imageRepository.findDetailById(1L))
                .thenReturn(Optional.of(image));

        // when
        ImageDetailResponse response = imageService.getImage(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.templateId()).isEqualTo(10L);
    }

}
