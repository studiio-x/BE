package net.studioxai.studioxBe.domain.folder.dto;

import java.util.List;

public record FolderResponse(
        Long folderId,
        String name,
        List<String> images
) {
    public static FolderResponse create(Long folderId, String name, List<String> images){
        return new FolderResponse(folderId, name, images);
    }
}
