package net.studioxai.studioxBe.domain.auth.repository;

import net.studioxai.studioxBe.domain.auth.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.auth.entity.PasswordResetCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetCodeRepository extends CrudRepository<PasswordResetCode, String> {
}
