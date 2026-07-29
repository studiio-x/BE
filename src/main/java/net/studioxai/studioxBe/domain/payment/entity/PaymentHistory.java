package net.studioxai.studioxBe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String orderId;

    private String paymentKey;

    private long amount;

    private String method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String requestedAt;

    private String approvedAt;

    private String failureCode;

    private String failureMessage;

    private LocalDateTime paidAt;

    public static PaymentHistory createPaymentHistory(User user, String orderId, int amount) {
        return PaymentHistory.builder()
                .user(user)
                .orderId(orderId)
                .amount(amount)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentHistory(User user, String orderId, int amount) {
        this.user = user;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.READY;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFail() {
        this.status = PaymentStatus.FAILED;
        this.paidAt = LocalDateTime.now();
    }

    public void updatePaymentResult(
            String paymentKey,
            long amount,
            String method,
            PaymentStatus status,
            String requestedAt,
            String approvedAt,
            String failureCode,
            String failureMessage
    ) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;

        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED) {
            this.paidAt = LocalDateTime.now();
        }
    }
}
