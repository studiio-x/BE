package net.studioxai.studioxBe.domain.user.repository;

import net.studioxai.studioxBe.domain.user.entity.EmailVerificationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends CrudRepository<EmailVerificationToken, String> {
}
