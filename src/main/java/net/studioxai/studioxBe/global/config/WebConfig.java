package net.studioxai.studioxBe.global.config;

import net.studioxai.studioxBe.global.jwt.argument_resolver.LoginUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    @Value("${server.server-url}")
    private String SERVER_URL;

    @Value("${server.front-urls}")
    private String[] FRONT_URLS;

    private static final String[] LOCALHOSTS = {
            "http://localhost:3000",
            "http://localhost:8080",
            "https://localhost:3000",
            "https://localhost:8080"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(mergeOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    private String[] mergeOrigins() {
        String[] merged = new String[1 + FRONT_URLS.length + LOCALHOSTS.length];

        int idx = 0;
        merged[idx++] = SERVER_URL;

        System.arraycopy(FRONT_URLS, 0, merged, idx, FRONT_URLS.length);
        idx += FRONT_URLS.length;

        System.arraycopy(LOCALHOSTS, 0, merged, idx, LOCALHOSTS.length);

        return merged;
    }

}
