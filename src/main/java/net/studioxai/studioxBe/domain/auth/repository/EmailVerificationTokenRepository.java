package net.studioxai.studioxBe.domain.auth.repository;

import net.studioxai.studioxBe.domain.auth.entity.EmailVerificationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends CrudRepository<EmailVerificationToken, String> {
}
