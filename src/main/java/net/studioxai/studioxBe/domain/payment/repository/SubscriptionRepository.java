package net.studioxai.studioxBe.domain.payment.repository;

import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("""
    select s.id
    from Subscription s
    where s.nextBillingAt < :startOfTomorrow
      and s.id > :lastId
      and (
            s.nextBillingRetryAt is null
            or s.nextBillingRetryAt <= :now
          )
    order by s.id asc
    """)
    List<Long> findDailyBillingTargetIds(
            LocalDateTime startOfTomorrow,
            LocalDateTime now,
            Long lastId,
            Pageable pageable
    );

    @Query("""
        select s
        from Subscription s
        where s.user = :user
        order by s.createdAt desc
        limit 1
        """)
    Optional<Subscription> findLatestByUser(@Param("user") User user);

    @Query("""
    select s
    from Subscription s
    join fetch s.user
    where s.status = :status
      and s.currentPeriodEnd <= :now
    """)
    List<Subscription> findExpiredSubscriptions(
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now
    );
}

