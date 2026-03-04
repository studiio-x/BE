package net.studioxai.studioxBe.domain.auth.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
class SecureResetCodeGenerator implements ResetCodeGenerator {
    private final SecureRandom random = new SecureRandom();
    public String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}