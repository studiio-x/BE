package net.studioxai.studioxBe.domain.auth.repository;

import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedEmailRepository extends CrudRepository<VerifiedEmail, String> {
}
