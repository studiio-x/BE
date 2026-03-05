package net.studioxai.studioxBe.global.config;

import net.studioxai.studioxBe.infra.ai.gemini.GeminiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class AiPropertiesConfig {

    @Bean
    public Executor geminiExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}

