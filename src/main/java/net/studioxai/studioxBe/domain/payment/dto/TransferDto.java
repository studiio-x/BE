package net.studioxai.studioxBe.domain.payment.dto;

public record TransferDto(
        String bankName,
        String bankAccountNumber
) {
}
