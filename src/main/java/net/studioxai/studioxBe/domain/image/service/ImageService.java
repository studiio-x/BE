package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.dto.request.ImageGenerateRequest;
import net.studioxai.studioxBe.domain.image.dto.response.ImageGenerateResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final TemplateRepository templateRepository;
    private final CutoutImageRepository cutoutImageRepository;
    private final NanobananaClient nanobananaClient;

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

            if (folder == null) {
                continue;
            }

            Long folderId = folder.getId();

            List<String> urls = result.computeIfAbsent(
                    folderId,
                    k -> new ArrayList<>()
            );

            if (urls.size() >= count) {
                continue;
            }

            urls.add(image.getImageUrl());
        }

        return result;
    }

    public ImageGenerateResponse generateImage(Long userId, ImageGenerateRequest request) {
        // 1. Template 조회
        Template template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        // 2. CutoutImage 생성 (folder = null)
        CutoutImage cutoutImage = CutoutImage.create(
                request.cutoutImageUrl(),
                template,
                null
        );
        cutoutImageRepository.save(cutoutImage);

        // 3. AI 호출
        NanobananaGenerateResponse aiResponse =
                nanobananaClient.generateImage(
                        cutoutImage.getCutoutImageUrl(),
                        template.getImageUrl(),
                        request.prompt()
                );

        // 4. Image 저장
        Image image = Image.create(
                cutoutImage,
                aiResponse.outputImageUrl()
        );
        imageRepository.save(image);

        // 5. 응답
        return new ImageGenerateResponse(
                image.getId(),
                image.getImageUrl()
        );
    }


}
