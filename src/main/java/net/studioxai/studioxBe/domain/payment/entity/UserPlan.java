package net.studioxai.studioxBe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;
import org.springframework.security.core.parameters.P;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_plans")
public class UserPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_plan_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "credit", nullable = false)
    private int credit;

    @Column(name = "storage", nullable = false)
    private long storage;

    @Column(name = "reference", nullable = false)
    private int reference;

    @Column(name = "team_size", nullable = false)
    private int teamSize;

    public static UserPlan createFree(User user) {
        return UserPlan.builder()
                .user(user)
                .plan(Plan.FREE)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private UserPlan(User user, Plan plan) {
        this.user = user;
        this.credit = 0;
        this.storage = 0;
        this.reference = 0;
        this.teamSize = plan.getTeamSize();
    }

    public void montlyInitialize() {
        this.credit = 0;
        this.reference = 0;
    }

}
