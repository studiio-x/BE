package net.studioxai.studioxBe.domain.payment.repository;

import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingKeyRepository extends JpaRepository<BillingKey, Long> {

}
