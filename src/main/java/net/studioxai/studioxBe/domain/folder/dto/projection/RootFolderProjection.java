package net.studioxai.studioxBe.domain.folder.dto.projection;

import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;

public interface RootFolderProjection {
    Long getFolderId();
    String getName();
    Integer getIsOwner();
}
