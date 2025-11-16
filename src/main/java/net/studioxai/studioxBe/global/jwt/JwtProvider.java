package net.studioxai.studioxBe.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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

    public JwtProvider(JwtProperties props) {
        this.signingKey =
                Keys.hmacShaKeyFor(props.secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessExpSeconds = props.accessTokenExpirationMs();
        this.refreshExpSeconds = props.refreshTokenExpirationMs();
    }

    public String createAccessToken(String userId) {
        return createToken(userId, "access", accessExpSeconds);
    }

    public String createRefreshToken(String userId) {
        return createToken(userId, "refresh", refreshExpSeconds);

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
                .setSubject(userId)
                .setClaims(Map.of("category", category))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
