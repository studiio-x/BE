package net.studioxai.studioxBe.infra.ai.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.gemini")
public record GeminiProperties(
        String baseUrl,
        String apiKey,
        String model
) {
}

