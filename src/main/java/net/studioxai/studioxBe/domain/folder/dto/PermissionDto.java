package net.studioxai.studioxBe.domain.folder.dto;

import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;

public record PermissionDto (
        Permission permission,
        boolean canWrite
) {
    public static PermissionDto create(Permission permission) {
        if (permission == Permission.READ) {
            return new PermissionDto(permission, false);
        }
        return new PermissionDto(permission, true);
    }
}
