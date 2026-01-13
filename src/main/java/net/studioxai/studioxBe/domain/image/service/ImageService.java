package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.folder.repository.FolderRepository;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.infra.ai.nanobanana.NanobananaClient;
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
    private final FolderRepository folderRepository;
    private final TemplateRepository templateRepository;
    private final NanobananaClient nanobananaClient;

    public List<String> getImagesByFolder(Folder folder, int count) {
        Pageable limit = PageRequest.of(0, count);
        return imageRepository.findByFolderOrderByCreatedAt(folder, limit)
                .stream()
                .map(Image::getImageUrl)
                .toList();
    }

    public Map<Long, List<String>> getImagesByFolders(List<Folder> folders, int count) {
        List<Image> images = imageRepository.findByFolderInOrderByFolderIdAndCreatedAtDesc(folders);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Image image : images) {
            Long folderId = image.getFolder().getId();

            List<String> urls = result.computeIfAbsent(folderId, k -> new ArrayList<>());

            if (urls.size() >= count) {
                continue;
            }

            urls.add(image.getImageUrl());
        }

        return result;
    }

    public Image generateAdImage(Long userId, Long folderId, Long templateId, String rawImageUrl) {

        // 1. Folder 조회 -> 빼고
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.FOLDER_NOT_FOUND));

        // 2. Template 조회
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ImageExceptionHandler(ImageErrorCode.TEMPLATE_NOT_FOUND));

        // 3. AI 프롬프트
        String prompt = """
                Create a photorealistic advertisement image.
                Blend the product image naturally with the reference style.
                Keep product shape and logo intact.
                Maintain clean, premium composition.
                """;

        // 4. AI 호출
        var aiResult = nanobananaClient.generateImage(
                rawImageUrl,
                template.getImageUrl(),
                prompt
        );

        // 5. Image 생성 (Folder 패턴과 동일) -> image url 어떻게 저장하는지
        Image image = Image.create(
                folder,
                template,
                rawImageUrl,
                aiResult.outputImageUrl()
        );

        // 6. 저장
        return imageRepository.save(image);
    }

}
