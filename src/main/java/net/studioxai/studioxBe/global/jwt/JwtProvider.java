package net.studioxai.studioxBe.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.studioxai.studioxBe.infra.redis.entity.RefreshToken;
import net.studioxai.studioxBe.infra.redis.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {
    private final SecretKey signingKey;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtProvider(JwtProperties props, RefreshTokenRepository refreshTokenRepository) {
        this.signingKey =
                Keys.hmacShaKeyFor(props.secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessExpSeconds = props.accessTokenExpirationMs();
        this.refreshExpSeconds = props.refreshTokenExpirationMs();
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAccessToken(Long userId) {
        return createToken(String.valueOf(userId), "access", accessExpSeconds);
    }

    public String createRefreshToken(Long userId) {
        String refreshToken = createToken(String.valueOf(userId), "refresh", refreshExpSeconds);
        RefreshToken refresh = RefreshToken.create(refreshToken, userId, refreshExpSeconds);
        refreshTokenRepository.save(refresh);

        return refreshToken;
    }

    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(String userId, String category, Long expirationSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("category", category)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
