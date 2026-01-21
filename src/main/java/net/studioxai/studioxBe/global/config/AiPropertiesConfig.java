package net.studioxai.studioxBe.global.config;

import net.studioxai.studioxBe.infra.ai.gemini.GeminiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class AiPropertiesConfig {
}

