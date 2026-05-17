package net.studioxai.studioxBe.domain.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.studioxai.studioxBe.domain.payment.dto.ThreeDsValueDto;

public record BillingKeyCardCreateRequest (
        @NotBlank(message = "카드 만료 월은 필수입니다.")
        @Pattern(
                regexp = "^(0[1-9]|1[0-2])$",
                message = "카드 만료 월은 01~12 형식이어야 합니다."
        )
        String cardExpirationMonth,

        @NotBlank(message = "카드 만료 연도는 필수입니다.")
        @Pattern(
                regexp = "^\\d{2}$",
                message = "카드 만료 연도는 YY 형식의 숫자 2자리여야 합니다."
        )
        String cardExpirationYear,

        @NotBlank(message = "카드 번호는 필수입니다.")
        @Pattern(
                regexp = "^\\d{12,19}$",
                message = "카드 번호는 숫자 12~19자리여야 합니다."
        )
        String cardNumber,

        @NotBlank(message = "카드 비밀번호 앞 2자리는 필수입니다.")
        @Pattern(
                regexp = "^\\d{2}$",
                message = "카드 비밀번호는 숫자 2자리여야 합니다."
        )
        String cardPassword,

        @NotBlank(message = "고객 식별번호는 필수입니다.")
        @Pattern(
                regexp = "^(\\d{6}|\\d{10})$",
                message = "고객 식별번호는 생년월일 6자리 또는 사업자등록번호 10자리여야 합니다."
        )
        String customerIdentityNumber,

        @NotBlank(message = "customerKey는 필수입니다.")
        @Size(min = 2, max = 50, message = "customerKey는 2자 이상 50자 이하여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9\\-_=\\.@]+$",
                message = "customerKey는 영문, 숫자, -, _, =, ., @ 만 사용할 수 있습니다."
        )
        String customerKey,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String customerEmail,

        @Size(max = 100, message = "고객 이름은 100자 이하여야 합니다.")
        String customerName,

        @Valid
        ThreeDsValueDto cavv,

        @Valid
        ThreeDsValueDto eci,

        @Valid
        ThreeDsValueDto xid
) {
}
