package net.studioxai.studioxBe.infra.redis.repository;

import net.studioxai.studioxBe.infra.redis.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
