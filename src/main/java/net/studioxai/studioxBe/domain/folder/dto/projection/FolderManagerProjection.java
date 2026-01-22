package net.studioxai.studioxBe.domain.folder.dto.projection;

import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;

public interface FolderManagerProjection {
    Long getUserId();
    String getProfileUrl();
    String getUsername();
    String getEmail();
    Permission getPermission();
}
