package net.studioxai.studioxBe.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.folder.service.FolderManagerService;
import net.studioxai.studioxBe.domain.image.dto.request.VideoGenerateRequestDto;
import net.studioxai.studioxBe.domain.image.dto.response.VideoGenerateResponseDto;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.domain.image.entity.enums.FileType;
import net.studioxai.studioxBe.domain.image.entity.enums.MotionType;
import net.studioxai.studioxBe.domain.image.entity.enums.QualityType;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.repository.ProjectRepository;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiOmniVideoClient;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final GeminiOmniVideoClient geminiOmniVideoClient;
    private final S3ImageUploader s3ImageUploader;
    private final FolderManagerService folderManagerService;
    private final ProjectRepository projectRepository;
    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final S3UrlHandler s3UrlHandler;

    public VideoGenerateResponseDto generateVideo(Long userId, VideoGenerateRequestDto request, MotionType motionType, QualityType qualityType) {
        folderManagerService.isUserWritable(userId, request.folderId());

        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCode.FOLDER_NOT_FOUND));

        int requiredCredits = qualityType.getCreditCost();

        // 1. cutoutObjectKey(배경제거 이미지)가 존재하면 우선 사용, 없으면 imageObjectKey 사용
        String targetObjectKey = selectTargetObjectKey(request);

        // 2. S3Client를 사용하여 S3에서 직접 이미지 파일 다운로드 및 Base64 인코딩
        String base64Image = downloadS3ObjectAndEncode(targetObjectKey);

        // 3. 프롬프트 구성
        String prompt = String.format(
                "Create a high quality product showcase video in %s resolution. %s " +
                        "Maintain the product's original details and scale. Professional studio lighting.",
                qualityType.getResolution(),
                motionType.getPromptInstruction()
        );

        // 4. 비디오 결과물이 저장될 S3 Key 생성
        String videoS3Key = "videos/generated/" + userId + "/"  + UUID.randomUUID() + ".mp4";

        log.info("비디오 생성 요청 - SelectedKey: {}, Motion: {}, Quality: {}", targetObjectKey, motionType, qualityType);

        // 5. Gemini Omni 비디오 생성 및 S3 업로드
        geminiOmniVideoClient.generateAndUploadToS3(base64Image, prompt, videoS3Key);

        Project project = Project.create(null, null, folder, FileType.VIDEO);
        projectRepository.save(project);

        Image image = Image.create(project, videoS3Key);
        imageRepository.save(image);

        project.updateThumbnailObjectKey(request.imageObjectKey());

        return new VideoGenerateResponseDto(videoS3Key, image.getId(), requiredCredits, "SUCCESS");
    }

    private String selectTargetObjectKey(VideoGenerateRequestDto request) {
        if (request.imageObjectKey() != null && !request.imageObjectKey().isBlank()) {
            return request.imageObjectKey();
        }
        throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
    }

    private String downloadS3ObjectAndEncode(String objectKey) {
        try {
            byte[] imageBytes = s3ImageUploader.downloadBytes(objectKey);
            if (imageBytes == null || imageBytes.length == 0) {
                throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
            }
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("S3 ObjectKey로 이미지 다운로드 실패 (Key: {}): {}", objectKey, e.getMessage());
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    public S3Url getVideoImageUrl() {
        return s3UrlHandler.handle("videos/raw");
    }
}