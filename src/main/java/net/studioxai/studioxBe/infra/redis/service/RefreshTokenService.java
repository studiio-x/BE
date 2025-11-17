package net.studioxai.studioxBe.infra.redis.service;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.infra.redis.entity.RefreshToken;
import net.studioxai.studioxBe.infra.redis.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public void saveRefreshToken(String token, Long userId) {
        RefreshToken refreshToken = RefreshToken.create(token, userId, jwtProperties.refreshTokenExpirationMs());
        refreshTokenRepository.save(refreshToken);
    }
}
