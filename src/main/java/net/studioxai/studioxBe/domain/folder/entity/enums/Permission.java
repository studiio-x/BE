package net.studioxai.studioxBe.domain.folder.entity.enums;

import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;

public enum Permission {
    OWNER(true, true) {

        @Override
        public Permission toggle() {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.OWNER_PERMISSION_CHANGE_FORBIDDEN);
        }
    },
    READ(false, true) {
        @Override
        public Permission toggle() {
            return WRITE;
        }
    },
    WRITE(true, true) {
        @Override
        public Permission toggle() {
            return READ;
        }
    };

    public abstract Permission toggle();

    private final boolean canWrite;
    private final boolean canRead;

    Permission(boolean canWrite, boolean canRead) {
        this.canWrite = canWrite;
        this.canRead = canRead;
    }

    public boolean isWritable() {
        return canWrite;
    }

    public boolean isReadable() {
        return canRead;
    }

}
