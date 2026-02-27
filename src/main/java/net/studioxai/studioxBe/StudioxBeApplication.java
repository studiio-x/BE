package net.studioxai.studioxBe;

import net.studioxai.studioxBe.infra.ai.gemini.GeminiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@ConfigurationPropertiesScan
public class StudioxBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudioxBeApplication.class, args);
	}

}
