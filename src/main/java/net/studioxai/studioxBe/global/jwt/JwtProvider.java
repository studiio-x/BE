package net.studioxai.studioxBe.global.jwt;

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

    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long id, String accessToken) {
        return "";
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
