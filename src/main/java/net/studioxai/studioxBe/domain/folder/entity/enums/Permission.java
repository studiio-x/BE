package net.studioxai.studioxBe.domain.folder.entity.enums;

import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;

public enum Permission {
    OWNER {
        @Override
        public Permission toggle() {
            throw new FolderManagerExceptionHandler(FolderManagerErrorCode.OWNER_PERMISSION_CHANGE_FORBIDDEN);
        }
    },
    READ {
        @Override
        public Permission toggle() {
            return WRITE;
        }
    },
    WRITE {
        @Override
        public Permission toggle() {
            return READ;
        }
    };

    public abstract Permission toggle();
}
