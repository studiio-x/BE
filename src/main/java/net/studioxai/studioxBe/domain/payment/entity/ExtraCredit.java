package net.studioxai.studioxBe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.CreditOption;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "extra_credits")
public class ExtraCredit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_plan_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditOption credit;

    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;


    @Builder(access = AccessLevel.PRIVATE)
    private ExtraCredit(User user, CreditOption credit, LocalDateTime now) {
        this.user = user;
        this.credit = credit;
        this.currentPeriodStart = now;
        this.currentPeriodEnd = now.plusDays(60);
    }

    public static ExtraCredit create(User user, CreditOption credit) {
        LocalDateTime now = LocalDateTime.now();
        return ExtraCredit.builder()
                .user(user)
                .credit(credit)
                .now(now)
                .build();
    }
}
