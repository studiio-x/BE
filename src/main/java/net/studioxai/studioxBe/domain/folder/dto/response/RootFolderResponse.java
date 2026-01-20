package net.studioxai.studioxBe.domain.folder.dto.response;

public record RootFolderResponse(
        Long folderId,
        String name
) {
    public static RootFolderResponse create(Long folderId, String name) {
        return new RootFolderResponse(folderId, name);
    }
}
