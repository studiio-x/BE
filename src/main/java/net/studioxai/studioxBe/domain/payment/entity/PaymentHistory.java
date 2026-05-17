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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    private Plan plan;

    private String orderId;

    private String paymentKey;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String failureCode;

    private String failureMessage;

    private LocalDateTime paidAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentHistory(User user, Subscription subscription, Plan plan, String orderId, String paymentKey, int amout) {
        this.user = user;
        this.subscription = subscription;
        this.plan = plan;
        this.orderId = orderId;
        this.amount = amout;
        this.paymentKey = paymentKey;
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
}
