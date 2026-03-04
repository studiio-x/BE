package net.studioxai.studioxBe.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordCodeVerificationRequest (
        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank(message = "code는 6자리 숫자를 입력해주세요")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "올바른 인증 코드 형식이 아닙니다."
        )
        String code
) {
}
