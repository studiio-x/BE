package net.studioxai.studioxBe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_key_id")
    private BillingKey billingKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(columnDefinition="TEXT")
    private String cancelReason;

    private LocalDateTime startedAt;

    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;

    private LocalDateTime nextBillingAt;

    private LocalDateTime canceledAt;

    @Column(nullable = false)
    private boolean cancelAtPeriodEnd;

    @Builder
    private Subscription(User user, Plan plan, BillingKey billingKey) {
        LocalDateTime now = LocalDateTime.now();

        this.user = user;
        this.plan = plan;
        this.billingKey = billingKey;
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = now;
        this.currentPeriodStart = now;
        this.currentPeriodEnd = now.plusMonths(1);
        this.nextBillingAt = now.plusMonths(1);
        this.cancelAtPeriodEnd = false;
    }

    public void changePlan(Plan plan) {
        this.plan = plan;
    }

    public void renew() {
        LocalDateTime now = LocalDateTime.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = now;
        this.currentPeriodEnd = now.plusMonths(1);
        this.nextBillingAt = now.plusMonths(1);
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void cancelAtPeriodEnd() {
        this.cancelAtPeriodEnd = true;
        this.canceledAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public static Subscription createSubscription(User user, Plan plan, BillingKey billingKey) {
        return Subscription.builder()
                .user(user)
                .plan(plan)
                .billingKey(billingKey)
                .build();
    }

}
