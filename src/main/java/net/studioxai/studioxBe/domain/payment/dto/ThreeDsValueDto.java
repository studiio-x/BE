package net.studioxai.studioxBe.domain.payment.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ThreeDsValueDto (
        @Size(max = 2048, message = "masking 값은 2048자 이하여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9+/=_\\-.*]*$",
                message = "masking 값에 허용되지 않는 문자가 포함되어 있습니다."
        )
                String masking,

        @Size(max = 2048, message = "plain 값은 2048자 이하여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9+/=_\\-.*]*$",
                message = "plain 값에 허용되지 않는 문자가 포함되어 있습니다."
        )
        String plain
) {
}