package net.studioxai.studioxBe.infra.ai.nanobanana;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.nanobanana")
public record NanobananaProperties (String baseUrl, String apiKey, String model){
}
