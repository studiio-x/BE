package net.studioxai.studioxBe.domain.payment.dto.response;

import net.studioxai.studioxBe.domain.payment.dto.CardDto;
import net.studioxai.studioxBe.domain.payment.dto.FailureDto;

import java.math.BigDecimal;

public record PaymentApprovalResponse(
        String version,
        String paymentKey,
        String type,
        String orderId,
        String orderName,
        String mId,
        String currency,
        String method,
        BigDecimal totalAmount,
        String balanceAmount,
        String status,
        String requestedAt,
        String approvedAt,
        boolean useEscrow,
        String lastTransactionKey,
        int suppliedAmount,
        int vat,
        boolean cultureExpense,
        int taxFreeAmount,
        int taxExemptionAmount,
        CardDto card,
        boolean isPartialCancelable,
        String country,
        FailureDto failure
) {

}
