package net.studioxai.studioxBe.domain.folder.dto.response;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.PermissionDto;

import java.util.List;

public record FolderManagersResponse(
        PermissionDto myPermission,
        List<FolderManagerDto> managers
) {
    public static FolderManagersResponse create(PermissionDto myPermission, List<FolderManagerDto> managers) {
        return new FolderManagersResponse(myPermission, managers);
    }
}
