package net.studioxai.studioxBe.domain.folder.entity.enums;

import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;

public enum Permission {
    OWNER(true, true, true),
    FULL_ACCESS(true, true, true),
    READ(true, false, false),
    WRITE(true, true, false),;

    private final boolean canShare;
    private final boolean canWrite;
    private final boolean canRead;

    Permission(boolean canWrite, boolean canShare,boolean canRead) {
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
