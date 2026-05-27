package net.studioxai.studioxBe.domain.payment.entity.enums;

import lombok.Getter;

@Getter
public enum Plan {
    FREE(0, 100, mbToByte(5), true, false, false, 0, 1),
    BASIC(8, 300, mbToByte(10), false, true, false, 15, 1),
    STANDARD(24, 900, mbToByte(50), false, true, true, -1, 1),
    PRO(48, 3000, mbToByte(200), false, true, true, -1, 5)
    ;

    private final int price;
    private final int credit;
    private final long storageLimit;
    private final boolean hasWatermark;
    private final boolean canChat;
    private final boolean canVersioning;
    private final int maxReferences;
    private final int teamSize;

    Plan(int price, int credit, long storageLimit, boolean hasWatermark, boolean canChat, boolean canVersioning, int maxReferences, int teamSize) {
        this.price = price;
        this.credit = credit;
        this.storageLimit = storageLimit;
        this.hasWatermark = hasWatermark;
        this.canChat = canChat;
        this.canVersioning = canVersioning;
        this.maxReferences = maxReferences;
        this.teamSize = teamSize;
    }

    public static long mbToByte(long value) {
        return value * 1024 * 1024;
    }

    public boolean isFree() {
        return this == FREE;
    }


}
