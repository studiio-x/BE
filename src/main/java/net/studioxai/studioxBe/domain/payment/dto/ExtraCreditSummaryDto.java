package net.studioxai.studioxBe.domain.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

public record ExtraCreditSummaryDto (
        int totalCreditAmount,
        LocalDateTime nearestExpiredAt
) {
    public ExtraCreditSummaryDto(Long totalExtraCredit, LocalDateTime nearestExpiredAt) {
        this(Math.toIntExact(totalExtraCredit), nearestExpiredAt);
    }
}
