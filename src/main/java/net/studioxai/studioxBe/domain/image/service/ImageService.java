package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.exception.ProjectErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ProjectExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
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
    private final TemplateRepository templateRepository;
    private final FolderRepository folderRepository;
    private final ProjectRepository projectRepository;

    private final S3UrlHandler s3UrlHandler;
    private final S3ImageLoader s3ImageLoader;
    private final S3ImageUploader s3ImageUploader;

    private final GeminiImageClient geminiImageClient;

    private final FolderManagerService folderManagerService;
    private final ProjectService projectService;


    public PresignResponse issuePresign() {
        S3Url s3Url = s3UrlHandler.handle("images/raw");

        return PresignResponse.of(
                s3Url.getUploadUrl(),
                s3Url.getObjectKey()
        );
    }

    @Transactional
    public CutoutImageGenerateResponse generateCutoutImage(Long userId, CutoutImageGenerateRequest request) {

        //TODO: 결제 검증 로직 추가

        folderManagerService.isUserWritable(userId, request.folderId());

        String rawImageBase64 = s3ImageLoader.loadAsBase64(request.rawObjectKey());

        String cutoutBase64 = geminiImageClient.removeBackground(rawImageBase64);
        byte[] cutoutBytes = Base64.getDecoder().decode(cutoutBase64);

        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND));

        Project project = Project.create(null, null, folder);
        projectRepository.save(project);

        String cutoutImageObjectKey = "images/" + project.getId() + "/cutout/" + UUID.randomUUID() + ".png";

        s3ImageUploader.upload(cutoutImageObjectKey, cutoutBytes);

        project.updateCutoutImageObjectKey(cutoutImageObjectKey);
        project.updatethumbnailObjectKey(cutoutImageObjectKey);

        return CutoutImageGenerateResponse.of(project.getId(), cutoutImageObjectKey);
    }

    @Transactional
    public ImageGenerateResponse generateImage(Long userId, ImageGenerateRequest request) {

        //TODO: 결제 검증 로직 추가

        Project project = projectService.getProjectById(request.projectId());

        folderManagerService.isUserWritable(userId, project.getFolder().getId());

        Template template = templateRepository.findById(request.templateId())
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        String cutoutBase64 = s3ImageLoader.loadAsBase64(request.cutoutImageObjectKey());
        String templateBase64 = s3ImageLoader.loadAsBase64(template.getImageObjectKey());

        String compositeBase64 = geminiImageClient.generateCompositeImage(cutoutBase64, templateBase64);
        byte[] imageBytes = Base64.getDecoder().decode(compositeBase64);

        String imageObjectKey = "images/" + project.getId() + "/result/" + UUID.randomUUID() + ".png";
        s3ImageUploader.upload(imageObjectKey, imageBytes);

        project.updateTemplate(template);
        project.updatethumbnailObjectKey(imageObjectKey);

        Image image = Image.create(project, imageObjectKey);
        imageRepository.save(image);

        return ImageGenerateResponse.of(image);
    }


    public ImageDetailResponse getImage(Long imageId) {
        Image image = imageRepository.findDetailById(imageId)
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.IMAGE_NOT_FOUND));

        return ImageDetailResponse.from(image);
    }

}
