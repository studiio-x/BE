package net.studioxai.studioxBe.domain.image.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QualityType {
    STANDARD(1, "720p"),
    HIGH(5, "1080p");

    private final int creditCost;
    private final String resolution;
}