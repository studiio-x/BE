package net.studioxai.studioxBe.domain.folder.entity.enums;

import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;

public enum Permission {
    OWNER(true, true, false),
    READ(true, false, false),
    WRITE(true, true, false),
    FULL_ACCESS(true, true, true),;

    private final boolean canWrite;
    private final boolean canRead;
    private final boolean canShare;

    Permission(boolean canWrite, boolean canRead, boolean canShare) {
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
