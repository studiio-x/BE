package net.studioxai.studioxBe.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.studioxai.studioxBe.domain.payment.dto.CardDto;
import net.studioxai.studioxBe.domain.payment.dto.TransferDto;

import java.util.List;

public record BillingKeyResponse (
        @JsonProperty("mId")
        String mId,

        String customerKey,

        String authenticatedAt,

        String method,

        String billingKey,

        CardDto card,

        List<TransferDto> transfers
) {

}
