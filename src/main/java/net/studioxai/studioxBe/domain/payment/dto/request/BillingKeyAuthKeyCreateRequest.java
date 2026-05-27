package net.studioxai.studioxBe.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BillingKeyAuthKeyCreateRequest(
        @NotBlank(message = "authKey는 필수입니다.")
        @Size(max = 300, message = "authKey는 300자 이하여야 합니다.")
        String authKey,

        @NotBlank(message = "customerKey는 필수입니다.")
        @Size(min = 2, max = 300, message = "customerKey는 2자 이상 300자 이하여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9\\-_=\\.@]+$",
                message = "customerKey는 영문, 숫자, -, _, =, ., @ 만 사용할 수 있습니다."
        )
        String customerKey
) {
}
