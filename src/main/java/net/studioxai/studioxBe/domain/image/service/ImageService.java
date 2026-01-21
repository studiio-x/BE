package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.dto.request.CutoutRequest;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.*;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.CutoutImage;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.CutoutImageRepository;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final CutoutImageRepository cutoutImageRepository;
    private final TemplateRepository templateRepository;

    private final S3UrlHandler s3UrlHandler;
    private final S3Client s3Client;
    private final GeminiImageClient geminiImageClient;
    private final S3ImageLoader s3ImageLoader;

    @Value("${BUCKET_NAME}")
    private String bucket;


    public List<String> getImagesByFolder(Folder folder, int count) {
        Pageable limit = PageRequest.of(0, count);
        return imageRepository.findByFolder(folder, limit)
                .stream()
                .map(Image::getImageUrl)
                .toList();
    }

    public Map<Long, List<String>> getImagesByFolders(List<Folder> folders, int count) {
        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Image image : imageRepository.findByFolders(folders)) {
            Folder folder = image.getCutoutImage().getFolder();
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
                s3Url.getObjectKey()
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

        return CutoutResponse.of(cutoutObjectKey);
    }

    @Transactional
    public ImageGenerateResponse generate(Long userId, ImageGenerateRequest request) {

        CutoutImage cutoutImage =
                cutoutImageRepository.findById(request.cutoutImageId())
                        .orElseThrow(() ->
                                new ImageExceptionHandler(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND));

        Template template =
                templateRepository.findById(request.templateId())
                        .orElseThrow(() ->
                                new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        // 1. 이미지 Base64 로드
        String cutoutBase64 =
                s3ImageLoader.loadAsBase64(cutoutImage.getCutoutImageUrl());

        String templateBase64 =
                s3ImageLoader.loadAsBase64(template.getImageUrl());

        // 2. Gemini 합성 요청
        String resultBase64 =
                geminiImageClient.generateCompositeImage(
                        cutoutBase64,
                        templateBase64,
                        request.prompt()
                );

        byte[] resultBytes = Base64.getDecoder().decode(resultBase64);

        // 3. S3 업로드
        String resultKey =
                "images/result/" + UUID.randomUUID() + ".png";

        uploadToS3(resultKey, resultBytes);

        // 4. DB 저장
        Image image = Image.create(cutoutImage, resultKey);
        imageRepository.save(image);

        return ImageGenerateResponse.of(image);
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
        CutoutImage cutoutImage =
                cutoutImageRepository.findWithTemplateAndFolderById(cutoutImageId)
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
