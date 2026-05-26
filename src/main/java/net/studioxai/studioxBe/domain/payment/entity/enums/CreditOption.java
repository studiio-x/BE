package net.studioxai.studioxBe.domain.payment.entity.enums;

import lombok.Getter;

@Getter
public enum CreditOption {
    CREDIT_100(4, 100),
    CREDIT_200(8, 200),
    CREDIT_500(20, 500),
    CREDIT_1200(48, 1200);

    final int price;

    final int creditAmount;

    CreditOption(int price, int creditAmount) {
        this.price = price;
        this.creditAmount = creditAmount;
    }

}
