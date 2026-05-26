package net.studioxai.studioxBe.domain.payment.repository;

import net.studioxai.studioxBe.domain.payment.dto.ExtraCreditSummaryDto;
import net.studioxai.studioxBe.domain.payment.entity.ExtraCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ExtraCreditRepository extends JpaRepository<ExtraCredit, Long> {
    @Query("""
        select new net.studioxai.studioxBe.domain.payment.dto.ExtraCreditSummaryDto(
            coalesce(sum(e.creditAmount), 0L),
            min(e.currentPeriodEnd)
        )
        from ExtraCredit e
        where e.user.id = :userId
          and e.currentPeriodStart <= :now
          and e.currentPeriodEnd > :now
    """)
    ExtraCreditSummaryDto findAvailableCreditSummary(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );
}
