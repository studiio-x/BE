package net.studioxai.studioxBe.domain.payment.dto;

public record CardDto(
        String issuerCode,
        String acquirerCode,
        String number,
        String cardType,
        String ownerType
) {
}
