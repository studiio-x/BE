package net.studioxai.studioxBe.domain.folder.dto;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

import java.util.List;

public record FoldersDto (
    Long folderId,
    String folderName,
    @ImageUrl List<String> images
) {
    public static FoldersDto create(
            Long folderId,
            String folderName,
            List<String> images
    ) {
        return new FoldersDto(
                folderId,
                folderName,
                images
        );
    }
}
