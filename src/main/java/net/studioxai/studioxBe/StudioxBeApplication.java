package net.studioxai.studioxBe;

import net.studioxai.studioxBe.infra.ai.gemini.GeminiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class StudioxBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudioxBeApplication.class, args);
	}

}
