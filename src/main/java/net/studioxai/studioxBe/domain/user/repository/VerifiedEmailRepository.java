package net.studioxai.studioxBe.domain.user.repository;

import net.studioxai.studioxBe.domain.user.entity.VerifiedEmail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedEmailRepository extends CrudRepository<VerifiedEmail, String> {
}
