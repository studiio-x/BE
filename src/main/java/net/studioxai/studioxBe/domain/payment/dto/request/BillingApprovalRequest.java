package net.studioxai.studioxBe.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.user.entity.User;

import java.math.BigDecimal;

public record BillingApprovalRequest(
        @NotBlank
        String billingKey,
        @NotBlank
        long amount,
        @NotBlank
        String customerKey,
        @NotBlank
        String orderId,
        @NotBlank
        String orderName,
        String customerEmail,
        String customerName,
        String customerIp,
        int taxFreeAmount,
        int taxExemptionAmount
) {
        public static BillingApprovalRequest of(
                User user,
                Plan plan,
                long amount,
                BillingKey billingKey,
                PaymentHistory paymentHistory,
                String customerIp,
                int taxFreeAmount,
                int taxExemptionAmount
        ) {
                return new BillingApprovalRequest(
                        billingKey.getBillingKey(),
                        amount,
                        user.getCustomerKey(),
                        paymentHistory.getOrderId(),
                        plan.name(),
                        user.getEmail(),
                        user.getUsername(),
                        customerIp,
                        taxFreeAmount,
                        taxExemptionAmount
                );
        }
}
