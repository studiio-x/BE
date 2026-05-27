package net.studioxai.studioxBe.domain.payment.dto.response;

import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;

import java.time.LocalDateTime;

public record MyPlanResponse(
        int totalCredit,
        int subscriptionCredit,
        int extraCredit,
        LocalDateTime creditExpirationDate,
        int subscriptionPrice,
        LocalDateTime subscriptionPaymentDate,
        LocalDateTime subscriptionExpirationDate,
        Plan userPlan
){
}
