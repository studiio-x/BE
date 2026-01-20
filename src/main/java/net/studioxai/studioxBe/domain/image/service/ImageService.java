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
import net.studioxai.studioxBe.infra.ai.nanobanana.NanobananaClient;
import net.studioxai.studioxBe.infra.ai.nanobanana.NanobananaGenerateResponse;
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
    private final NanobananaClient nanobananaClient;
    private final RestTemplate restTemplate;


    @Value("${BUCKET_NAME}")
    private String bucket;

    @Value("${server.image-domain}")
    private String imageDomain;


    public List<String> getImagesByFolder(Folder folder, int count) {
        Pageable limit = PageRequest.of(0, count);

        return imageRepository.findByFolder(folder, limit)
                .stream()
                .map(Image::getImageUrl)
                .toList();
    }


    public Map<Long, List<String>> getImagesByFolders(List<Folder> folders, int count) {
        List<Image> images = imageRepository.findByFolders(folders);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Image image : images) {
            Folder folder = image.getCutoutImage().getFolder();

            if (folder == null) {continue;}

            Long folderId = folder.getId();

            List<String> urls = result.computeIfAbsent(
                    folderId,
                    k -> new ArrayList<>()
            );

            if (urls.size() >= count) {continue;}

            urls.add(image.getImageUrl());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public RawPresignResponse issueRawPresign(Long userId) {
        // TODO: userId 기반 rate limit, 권한 체크 등 가능

        S3Url s3Url = s3UrlHandler.handle("images/raw");
        return RawPresignResponse.of(s3Url.getUploadUrl(), s3Url.getObjectKey());
    }

    // 2~3) 검증 → AI 누끼 → cutout S3 업로드
    public CutoutResponse cutout(Long userId, CutoutRequest request) {

        // TODO 2) 검증(결제/쿼터/권한 등)

        // raw objectKey -> raw image url(접근 가능한 도메인)
        String rawImageUrl = toPublicUrl(request.rawObjectKey());

        // 2) AI에게 누끼 요청 (templateImageUrl은 null)
        NanobananaGenerateResponse aiResponse =
                nanobananaClient.generateImage(
                        rawImageUrl,
                        null,
                        "remove background"
                );

        String cutoutExternalUrl = aiResponse.outputImageUrl();

        // 3) AI 결과 이미지를 다운로드 -> 우리 S3에 저장
        byte[] cutoutBytes = downloadBytes(cutoutExternalUrl);

        String cutoutKey = "images/cutout/" + UUID.randomUUID() + ".png";

        putObjectToS3(cutoutKey, cutoutBytes, "image/png");

        return CutoutResponse.of(cutoutKey);
    }

    private String toPublicUrl(String objectKeyOrUrl) {
        if (objectKeyOrUrl.startsWith("http://") || objectKeyOrUrl.startsWith("https://")) {
            return objectKeyOrUrl;
        }
        return imageDomain + objectKeyOrUrl;
    }

    private byte[] downloadBytes(String url) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Failed to download cutout image from AI response url.");
            }
            return response.getBody();
        } catch (RestClientException e) {
            throw new ImageExceptionHandler(ImageErrorCode.AI_IMAGE_DOWNLOAD_FAILED);
        }
    }

    private void putObjectToS3(String objectKey, byte[] bytes, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new ImageExceptionHandler(ImageErrorCode.S3_UPLOAD_FAILED);

        }
    }

    public ImageGenerateResponse generate(
            Long userId,
            ImageGenerateRequest request
    ) {
        // 1. CutoutImage 조회
        CutoutImage cutoutImage = cutoutImageRepository.findById(request.cutoutImageId())
                .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.CUTOUT_IMAGE_NOT_FOUND));

        // 2. Template 조회
        Template template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ImageExceptionHandler(
                        ImageErrorCode.TEMPLATE_NOT_FOUND
                ));

        // TODO userId 권한 검증 (cutoutImage 소유자 등)

        // 3. AI 합성 요청
        NanobananaGenerateResponse aiResponse =
                nanobananaClient.generateImage(
                        toPublicUrl(cutoutImage.getCutoutImageUrl()),
                        template.getImageUrl(),
                        request.prompt()
                );

        // 4. AI 결과 다운로드
        byte[] resultBytes = downloadBytes(aiResponse.outputImageUrl());

        // 5. S3 업로드
        String resultKey = "images/result/" + UUID.randomUUID() + ".png";
        uploadToS3(resultKey, resultBytes);

        // 6. Image 엔티티 저장
        Image image = Image.create(
                cutoutImage,
                resultKey
        );
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
                        .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.IMAGE_NOT_FOUND));

        return ImageResponse.from(image);
    }



}
