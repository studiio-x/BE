package net.studioxai.studioxBe.domain.payment.dto;

import java.time.LocalDateTime;

public record ExtraCreditSummaryDto (
        int totalCreditAmount,
        LocalDateTime nearestExpiredAt
) {
}
