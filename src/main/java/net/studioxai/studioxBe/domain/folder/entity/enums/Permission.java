package net.studioxai.studioxBe.domain.folder.entity.enums;

import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;

public enum Permission {
    OWNER(true, true, true),
    FULL_ACCESS(true, true, true),
    WRITE(true, true, false),
    READ(true, false, false),
    ;

    private final boolean canShare;
    private final boolean canWrite;
    private final boolean canRead;

    Permission(boolean canRead, boolean canWrite, boolean canShare) {
        this.canWrite = canWrite;
        this.canRead = canRead;
        this.canShare = canShare;
    }

    public boolean isWritable() {
        return canWrite;
    }

    public boolean isReadable() {
        return canRead;
    }

    public boolean isShareable() { return canShare; }

}
