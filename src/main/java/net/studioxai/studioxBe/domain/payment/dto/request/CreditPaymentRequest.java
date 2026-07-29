package net.studioxai.studioxBe.domain.payment.dto.request;

public record CreditPaymentRequest (
        String paymentKey,
        String orderId,
        long amount
) {
}
