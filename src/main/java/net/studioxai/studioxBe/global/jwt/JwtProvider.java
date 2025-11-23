package net.studioxai.studioxBe.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.studioxai.studioxBe.infra.redis.repository.TokenRepository;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {
    private final SecretKey signingKey;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final long mailExpSeconds;

    public JwtProvider(JwtProperties props) {
        this.signingKey =
                Keys.hmacShaKeyFor(props.secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessExpSeconds = props.accessTokenExpirationMs() / 1000;
        this.refreshExpSeconds = props.refreshTokenExpirationMs() / 1000;
        this.mailExpSeconds = props.mailTokenExpirationMs() / 1000;
    }

    public String createAccessToken(Long userId) {
        return createToken(String.valueOf(userId), "access", accessExpSeconds);
    }

    public String createRefreshToken(Long userId) {
        return createToken(String.valueOf(userId), "refresh", refreshExpSeconds);
    }

    public String createEmailToken(String email) {
        return createToken(email, "email", mailExpSeconds);
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
