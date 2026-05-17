package net.studioxai.studioxBe.domain.payment.repository;

import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPlanRepository extends JpaRepository<UserPlan, Long> {
}
