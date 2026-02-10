package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutImageGenerateRequest;
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

import java.io.InputStream;
import java.net.URL;
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

    public PresignResponse issuePresign(Long userId) {
        S3Url s3Url = s3UrlHandler.handle("images/raw");

        return PresignResponse.of(
                s3Url.getUploadUrl(),
                s3Url.getObjectKey(),
                imageDomain + s3Url.getObjectKey()
        );
    }

    @Transactional
    public CutoutImageGenerateResponse generateCutoutImage(Long userId, CutoutImageGenerateRequest request) {

        String rawImageBase64 = s3ImageLoader.loadAsBase64(request.rawObjectKey());

        String cutoutBase64 = geminiImageClient.removeBackground(rawImageBase64);

        byte[] cutoutBytes = Base64.getDecoder().decode(cutoutBase64);

        String cutoutImageObjectKey = "images/cutout/" + UUID.randomUUID() + ".png";

        uploadToS3(cutoutImageObjectKey, cutoutBytes);

        return CutoutImageGenerateResponse.of(cutoutImageObjectKey, imageDomain + cutoutImageObjectKey);
    }

    @Transactional
    public ImageGenerateResponse generateImage(Long userId, ImageGenerateRequest request) {

        Template template = templateRepository.findById(request.templateId())
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        String cutoutBase64 = s3ImageLoader.loadAsBase64(request.cutoutImageObjectKey());

        String templateBase64 =
                loadUrlAsBase64(template.getImageUrl());

        String prompt = """
            Composite the provided cutout image naturally into the template image.
    
            Requirements:
            - Place the cutout subject realistically within the template
            - Match perspective, scale, and alignment
            - Preserve original colors and details
            - Do NOT add new objects
            - Do NOT alter the template background
            - Output must be a clean PNG
        """;

        String imageBase64 = geminiImageClient.generateCompositeImage(
                        cutoutBase64,
                        templateBase64,
                        prompt
                );

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        String imageObjectKey = "images/result/" + UUID.randomUUID() + ".png";

        uploadToS3(imageObjectKey, imageBytes);

        Project project = Project.create(
                request.cutoutImageObjectKey(),
                template,
                null
        );
        projectRepository.save(project);

        Image image = Image.create(
                project,
                imageObjectKey
        );
        imageRepository.save(image);

        project.updateRepresentativeImage(imageObjectKey);

        //return ImageGenerateResponse.of(image);
        return new ImageGenerateResponse(
                image.getId(),
                imageDomain + image.getImageObjectKey()
        );
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


    public ProjectDetailResponse getProject(Long projectId) {
        Project project = projectRepository.findWithTemplateAndFolderById(projectId)
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND));

        return ProjectDetailResponse.from(project);
    }

    public ImageDetailResponse getImage(Long imageId) {
        Image image = imageRepository.findDetailById(imageId)
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.IMAGE_NOT_FOUND));

        return ImageDetailResponse.from(image);
    }

    public String loadUrlAsBase64(String imageUrl) {
        try (InputStream is = new URL(imageUrl).openStream()) {
            byte[] bytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new ImageExceptionHandler(ImageErrorCode.TEMPLATE_LOAD_FAILED);
        }
    }
}
