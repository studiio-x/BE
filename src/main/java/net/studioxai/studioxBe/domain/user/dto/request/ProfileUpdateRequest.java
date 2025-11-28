package net.studioxai.studioxBe.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
        @NotBlank(message = "콜백 URL은 필수입니다.")
        @Pattern(
                regexp = "^(https?):\\/\\/([^:\\/\\s]+)(:([0-9]{1,5}))?(\\/.*)?$",
                message = "올바른 URL 형식이 아닙니다."
        )
        String profileImage
) {
}
