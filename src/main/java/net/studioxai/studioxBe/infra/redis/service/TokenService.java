package net.studioxai.studioxBe.infra.redis.service;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.infra.redis.entity.Token;
import net.studioxai.studioxBe.infra.redis.exception.TokenErrorCode;
import net.studioxai.studioxBe.infra.redis.exception.TokenExceptionHandler;
import net.studioxai.studioxBe.infra.redis.repository.TokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final JwtProperties jwtProperties;

    public void saveRefreshToken(String token, Long userId) {
        Token refreshToken = Token.create(token, userId, jwtProperties.refreshTokenExpirationMs() / 1000);
        tokenRepository.save(refreshToken);
    }

    public Token findByRefreshTokenOrThrow(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new TokenExceptionHandler(TokenErrorCode.INVALID_REFRESH_TOKEN)
        );
    }
}
