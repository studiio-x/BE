package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.CutoutResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ImageGenerateResponse;
import net.studioxai.studioxBe.domain.image.dto.response.RawPresignResponse;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    /* ===== Mock ===== */
    @Mock private ImageRepository imageRepository;
    @Mock private ProjectRepository cutoutImageRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private S3UrlHandler s3UrlHandler;
    @Mock private S3Client s3Client;
    @Mock private GeminiImageClient geminiImageClient;
    @Mock private S3ImageLoader s3ImageLoader;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
    }

    /* ======================================================
     * issueRawPresign
     * ====================================================== */

    @Test
    @DisplayName("[issueRawPresign] presigned url 발급")
    void issueRawPresign_success() {
        S3Url s3Url = S3Url.to("https://upload.url", "images/raw/a.png");
        given(s3UrlHandler.handle("images/raw")).willReturn(s3Url);

        RawPresignResponse response = imageService.issueRawPresign(1L);

        assertThat(response.uploadUrl()).isEqualTo("https://upload.url");
        assertThat(response.rawObjectKey()).isEqualTo("images/raw/a.png");
    }

    /* ======================================================
     * cutout
     * ====================================================== */

    @Test
    @DisplayName("[cutout] RAW → 누끼 → S3 저장")
    void cutout_success() {
        // given
        CutoutRequest request = new CutoutRequest("images/raw/raw.png");

        String validBase64 = Base64.getEncoder().encodeToString(new byte[]{1,2,3});

        given(s3ImageLoader.loadAsBase64(any()))
                .willReturn(validBase64);

        given(geminiImageClient.removeBackground(any()))
                .willReturn(validBase64);

        // when
        CutoutResponse response = imageService.cutout(1L, request);

        // then
        assertThat(response.cutoutObjectKey())
                .startsWith("images/cutout/");

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    /* ======================================================
     * generate
     * ====================================================== */

    @Test
    @DisplayName("[generate] cutout + template 합성 성공")
    void generate_success() {
        Project cutout = mock(Project.class);
        Template template = mock(Template.class);

        given(cutout.getCutoutImageUrl()).willReturn("images/cutout/a.png");
        given(template.getImageUrl()).willReturn("images/template/t.png");

        given(cutoutImageRepository.findById(1L))
                .willReturn(Optional.of(cutout));
        given(templateRepository.findById(2L))
                .willReturn(Optional.of(template));

        String cutoutBase64 = Base64.getEncoder().encodeToString(new byte[]{1});
        String templateBase64 = Base64.getEncoder().encodeToString(new byte[]{2});
        String resultBase64 = Base64.getEncoder().encodeToString(new byte[]{3});

        given(s3ImageLoader.loadAsBase64("images/cutout/a.png"))
                .willReturn(cutoutBase64);
        given(s3ImageLoader.loadAsBase64("images/template/t.png"))
                .willReturn(templateBase64);

        given(geminiImageClient.generateCompositeImage(
                cutoutBase64,
                templateBase64,
                "prompt"
        )).willReturn(resultBase64);

        ImageGenerateResponse response =
                imageService.generate(1L, new ImageGenerateRequest(1L, 2L, "prompt"));

        assertThat(response).isNotNull();
        verify(imageRepository).save(any(Image.class));
    }


    @Test
    @DisplayName("[generate] cutoutImage 없으면 예외")
    void generate_cutout_not_found() {
        given(cutoutImageRepository.findById(1L))
                .willReturn(Optional.empty());

        ImageGenerateRequest request =
                new ImageGenerateRequest(1L, 2L, "prompt");

        assertThatThrownBy(() -> imageService.generate(1L, request))
                .isInstanceOf(ImageExceptionHandler.class)
                .satisfies(e ->
                        assertThat(((ImageExceptionHandler) e).getErrorCode())
                                .isEqualTo(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND)
                );
    }
}
