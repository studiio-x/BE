package net.studioxai.studioxBe.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d\\W_]{8,35}$",
                message = "비밀번호는 8~35자이며 영문 대문자, 소문자, 숫자를 각각 1개 이상 포함해야 합니다."
        )
        String password
) {
}
