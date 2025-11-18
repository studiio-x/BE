package net.studioxai.studioxBe.infra.redis.repository;

import net.studioxai.studioxBe.infra.redis.entity.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {
}
