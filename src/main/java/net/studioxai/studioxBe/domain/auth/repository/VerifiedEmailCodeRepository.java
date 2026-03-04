package net.studioxai.studioxBe.domain.auth.repository;

import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmailCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedEmailCodeRepository extends CrudRepository<VerifiedEmailCode, String> {
}
