package net.studioxai.studioxBe.domain.payment.entity.enums;

import lombok.Getter;

@Getter
public enum CreditOption {
    CREDIT_100(4),
    CREDIT_200(8),
    CREDIT_500(20),
    CREDIT_1200(48);

    int price;

    CreditOption(int price) {
        this.price = price;
    }

}
