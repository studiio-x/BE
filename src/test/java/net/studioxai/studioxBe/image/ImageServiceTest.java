package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.CutoutResponse;
import net.studioxai.studioxBe.domain.image.dto.response.ImageGenerateResponse;
import net.studioxai.studioxBe.domain.image.entity.CutoutImage;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.CutoutImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.nanobanana.NanobananaClient;
import net.studioxai.studioxBe.infra.ai.nanobanana.NanobananaGenerateResponse;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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
    @Mock private CutoutImageRepository cutoutImageRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private S3UrlHandler s3UrlHandler;
    @Mock private S3Client s3Client;
    @Mock private NanobananaClient nanobananaClient;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(imageService, "imageDomain", "https://cdn.test/");
    }

    /* ======================================================
     * getImagesByFolder
     * ====================================================== */
    @Test
    @DisplayName("[getImagesByFolder] 폴더와 개수를 받아 이미지 URL 리스트를 반환한다")
    void getImagesByFolder_success() {
        Folder folder = mock(Folder.class);

        Image img1 = mock(Image.class);
        Image img2 = mock(Image.class);

        given(img1.getImageUrl()).willReturn("url1");
        given(img2.getImageUrl()).willReturn("url2");

        given(imageRepository.findByFolder(eq(folder), any(Pageable.class)))
                .willReturn(List.of(img1, img2));

        List<String> result = imageService.getImagesByFolder(folder, 2);

        assertThat(result).containsExactly("url1", "url2");
    }

    /* ======================================================
     * getImagesByFolders
     * ====================================================== */
    @Test
    @DisplayName("[getImagesByFolders] 여러 폴더를 폴더별로 그룹핑한다")
    void getImagesByFolders_group() {
        Folder folder1 = mock(Folder.class);
        Folder folder2 = mock(Folder.class);

        given(folder1.getId()).willReturn(1L);
        given(folder2.getId()).willReturn(2L);

        CutoutImage c1 = mock(CutoutImage.class);
        CutoutImage c2 = mock(CutoutImage.class);
        CutoutImage c3 = mock(CutoutImage.class);

        given(c1.getFolder()).willReturn(folder1);
        given(c2.getFolder()).willReturn(folder1);
        given(c3.getFolder()).willReturn(folder2);

        Image i1 = mock(Image.class);
        Image i2 = mock(Image.class);
        Image i3 = mock(Image.class);

        given(i1.getCutoutImage()).willReturn(c1);
        given(i2.getCutoutImage()).willReturn(c2);
        given(i3.getCutoutImage()).willReturn(c3);

        given(i1.getImageUrl()).willReturn("a");
        given(i2.getImageUrl()).willReturn("b");
        given(i3.getImageUrl()).willReturn("c");

        given(imageRepository.findByFolders(List.of(folder1, folder2)))
                .willReturn(List.of(i1, i2, i3));

        Map<Long, List<String>> result =
                imageService.getImagesByFolders(List.of(folder1, folder2), 2);

        assertThat(result.get(1L)).containsExactly("a", "b");
        assertThat(result.get(2L)).containsExactly("c");
    }

    /* ======================================================
     * issueRawPresign
     * ====================================================== */
    @Test
    @DisplayName("[issueRawPresign] raw 이미지 업로드용 presigned url 발급")
    void issueRawPresign_success() {
        S3Url s3Url = S3Url.to("https://upload.url", "images/raw/abc.png");
        given(s3UrlHandler.handle("images/raw")).willReturn(s3Url);

        var response = imageService.issueRawPresign(1L);

        assertThat(response.uploadUrl()).isEqualTo("https://upload.url");
        assertThat(response.rawObjectKey()).isEqualTo("images/raw/abc.png");
    }

    /* ======================================================
     * cutout
     * ====================================================== */
    @Test
    @DisplayName("[cutout] raw 이미지를 AI에 보내 누끼를 따고 S3에 저장한다")
    void cutout_success() {
        CutoutRequest request = new CutoutRequest("images/raw/raw.png");

        given(nanobananaClient.generateImage(any(), isNull(), any()))
                .willReturn(new NanobananaGenerateResponse(
                        "req-1", "https://ai.cut.png"
                ));

        given(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .willReturn(ResponseEntity.ok(new byte[]{1, 2, 3}));

        CutoutResponse response = imageService.cutout(1L, request);

        assertThat(response.cutoutObjectKey())
                .startsWith("images/cutout/");

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    @Test
    @DisplayName("[cutout] AI 이미지 다운로드 실패 시 예외")
    void cutout_download_fail() {
        CutoutRequest request = new CutoutRequest("images/raw/raw.png");

        given(nanobananaClient.generateImage(any(), isNull(), any()))
                .willReturn(new NanobananaGenerateResponse(
                        "req-2", "https://ai.fail.png"
                ));

        given(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .willThrow(new RestClientException("fail"));

        assertThatThrownBy(() -> imageService.cutout(1L, request))
                .isInstanceOf(ImageExceptionHandler.class)
                .satisfies(e -> {
                    ImageExceptionHandler ex = (ImageExceptionHandler) e;
                    assertThat(ex.getErrorCode())
                            .isEqualTo(ImageErrorCode.AI_IMAGE_DOWNLOAD_FAILED);
                });
    }

    /* ======================================================
     * generate
     * ====================================================== */
    @Test
    @DisplayName("[generate] cutout + template으로 최종 이미지 생성")
    void generate_success() {
        CutoutImage cutout = mock(CutoutImage.class);
        Template template = mock(Template.class);

        given(cutout.getCutoutImageUrl()).willReturn("images/cutout/c.png");
        given(template.getImageUrl()).willReturn("https://template.png");

        given(cutoutImageRepository.findById(1L))
                .willReturn(Optional.of(cutout));
        given(templateRepository.findById(2L))
                .willReturn(Optional.of(template));

        given(nanobananaClient.generateImage(any(), any(), any()))
                .willReturn(new NanobananaGenerateResponse(
                        "req-3", "https://ai.result.png"
                ));

        given(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .willReturn(ResponseEntity.ok(new byte[]{1}));

        ImageGenerateRequest request =
                new ImageGenerateRequest(1L, 2L, "prompt");

        ImageGenerateResponse response =
                imageService.generate(1L, request);

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
                .satisfies(e -> {
                    ImageExceptionHandler ex = (ImageExceptionHandler) e;
                    assertThat(ex.getErrorCode())
                            .isEqualTo(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND);
                });
    }
}
