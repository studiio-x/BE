package net.studioxai.studioxBe.domain.image.dto.request;

import net.studioxai.studioxBe.domain.image.entity.enums.MotionType;
import net.studioxai.studioxBe.domain.image.entity.enums.QualityType;

public record VideoGenerateRequestDto(
        String imageObjectKey,
        Long folderId
) {}
