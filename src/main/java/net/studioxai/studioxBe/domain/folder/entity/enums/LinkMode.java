package net.studioxai.studioxBe.domain.folder.entity.enums;

public enum LinkMode {
    LINK {
        @Override
        public LinkMode toggle() { return UNLINK; }
    },
    UNLINK {
        @Override
        public LinkMode toggle() { return LINK; }
    };

    public abstract LinkMode toggle();

    public boolean isLink() {
        return this == LINK;
    }
}
