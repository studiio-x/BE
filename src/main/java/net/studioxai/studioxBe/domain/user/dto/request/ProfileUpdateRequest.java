package net.studioxai.studioxBe.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
        @NotBlank(message = "프로필 이미지 입력은 필수입니다.")
        String profileImage
) {
}
