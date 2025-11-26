package net.studioxai.studioxBe.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank(message = "콜백 URL은 필수입니다.")
        @Pattern(
                regexp = "^(https?):\\/\\/([^:\\/\\s]+)(:([0-9]{1,5}))?(\\/.*)?$",
                message = "올바른 URL 형식이 아닙니다."
        )
        String callbackUrl
) {
}
