package net.studioxai.studioxBe.domain.folder.dto;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

import java.util.List;

public record FolderResponse(
        Long folderId,
        String name,
        @ImageUrl List<String> images
) {
    public static FolderResponse create(Long folderId, String name, List<String> images){
        return new FolderResponse(folderId, name, images);
    }
}
