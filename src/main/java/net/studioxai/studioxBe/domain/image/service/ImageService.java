package net.studioxai.studioxBe.domain.image.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
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
}
