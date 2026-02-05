package net.studioxai.studioxBe.domain.folder.dto;

public record RootFolderDto(
        Long folderId,
        String name
) {
    public static RootFolderDto create(Long folderId, String name) {
        return new RootFolderDto(folderId, name);
    }
}
