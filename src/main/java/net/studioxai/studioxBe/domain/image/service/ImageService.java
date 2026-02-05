package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;


import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProjectRepository projectRepository;
    private final TemplateRepository templateRepository;

    private final S3UrlHandler s3UrlHandler;
    private final S3Client s3Client;
    private final GeminiImageClient geminiImageClient;
    private final S3ImageLoader s3ImageLoader;

    @Value("${BUCKET_NAME}")
    private String bucket;

    @Value("${server.image-domain}")
    private String imageDomain;

    public Map<Long, List<String>> getImagesByFolders(List<Folder> folders, int count) {
        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Image image : imageRepository.findByFolders(folders)) {
            Folder folder = image.getProject().getFolder();
            if (folder == null) continue;

            result.computeIfAbsent(folder.getId(), k -> new ArrayList<>());

            List<String> urls = result.get(folder.getId());
            if (urls.size() < count) {
                urls.add(image.getImageUrl());
            }
        }
        return result;
    }


    public RawPresignResponse issueRawPresign(Long userId) {
        S3Url s3Url = s3UrlHandler.handle("images/raw");

        return RawPresignResponse.of(
                s3Url.getUploadUrl(),
                s3Url.getObjectKey(),
                imageDomain + s3Url.getObjectKey()
        );
    }

    @Transactional
    public CutoutResponse cutout(Long userId, CutoutRequest request) {

        // 1. S3 RAW 이미지 → Base64
        String rawImageBase64 =
                s3ImageLoader.loadAsBase64(request.rawObjectKey());

        // 2. Gemini 누끼 요청
        String cutoutBase64 =
                geminiImageClient.removeBackground(rawImageBase64);

        // 3. Base64 → byte[]
        byte[] cutoutBytes = Base64.getDecoder().decode(cutoutBase64);

        // 4. S3 저장
        String cutoutObjectKey =
                "images/cutout/" + UUID.randomUUID() + ".png";

        uploadToS3(cutoutObjectKey, cutoutBytes);

        return CutoutResponse.of(cutoutObjectKey, imageDomain + cutoutObjectKey);
    }

    @Transactional
    public ImageGenerateResponse generateResultImage(Long userId, ImageGenerateRequest request) {

        // 1. Template 조회 (이건 그대로)
        Template template =
                templateRepository.findById(request.templateId())
                        .orElseThrow(() ->
                                new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        // 2. S3에서 Base64 로드
        String cutoutBase64 =
                s3ImageLoader.loadAsBase64(request.cutoutObjectKey());

        String templateBase64 =
                s3ImageLoader.loadAsBase64(template.getImageUrl());

        // 3. 고정 프롬프트
        String fixedPrompt = """
            Composite the provided cutout image naturally into the template image.
    
            Requirements:
            - Place the cutout subject realistically within the template
            - Match perspective, scale, and alignment
            - Preserve original colors and details
            - Do NOT add new objects
            - Do NOT alter the template background
            - Output must be a clean PNG
        """;

        // 4. Gemini 합성
        String resultBase64 =
                geminiImageClient.generateCompositeImage(
                        cutoutBase64,
                        templateBase64,
                        fixedPrompt
                );

        byte[] resultBytes = Base64.getDecoder().decode(resultBase64);

        // 5. S3 업로드
        String resultKey = "images/result/" + UUID.randomUUID() + ".png";

        uploadToS3(resultKey, resultBytes);

        // 6. DB 저장
        Project cutoutImage = Project.create(
                resultKey,
                template,
                null    // folder 아직 없으면 null
        );
        projectRepository.save(cutoutImage);

        return ImageGenerateResponse.of(cutoutImage);
    }



    private void uploadToS3(String objectKey, byte[] bytes) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType("image/png")
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new ImageExceptionHandler(ImageErrorCode.S3_UPLOAD_FAILED);
        }
    }


    public CutoutImageResponse getCutoutImage(Long cutoutImageId) {
        Project cutoutImage =
                projectRepository.findWithTemplateAndFolderById(cutoutImageId)
                        .orElseThrow(() ->
                                new ImageExceptionHandler(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND));

        return CutoutImageResponse.from(cutoutImage);
    }

    public ImageResponse getImage(Long imageId) {
        Image image =
                imageRepository.findDetailById(imageId)
                        .orElseThrow(() ->
                                new ImageExceptionHandler(ImageErrorCode.IMAGE_NOT_FOUND));

        return ImageResponse.from(image);
    }
}
