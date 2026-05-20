package net.studioxai.studioxBe.domain.payment.repository;

import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("""
    select s
    from Subscription s
    join fetch s.user u
    join fetch s.billingKey b
    where s.status = 'ACTIVE'
      and s.nextBillingAt < :before
    order by s.nextBillingAt asc
    """)
    List<Subscription> findDailyBillingTargets(
            @Param("before") LocalDateTime before,
            Pageable pageable
    );
}

