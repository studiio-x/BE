package net.studioxai.studioxBe.global.config;

import net.studioxai.studioxBe.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.util.EnvironmentUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${server.server-url}")
    private String SERVER_URL;

    @Value("${server.front-urls}")
    private String[] FRONT_URLS;

    @Value("${swagger.user}")
    private String SWAGGER_USER;

    @Value("${swagger.password}")
    private String SWAGGER_PASSWORD;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final EnvironmentUtil environmentUtil;

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user =
                User.withUsername(SWAGGER_USER)
                        .password(passwordEncoder().encode(SWAGGER_PASSWORD))
                        .roles("SWAGGER")
                        .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final String[] SwaggerPatterns = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    private final String[] PermitAllPatterns = {
            "/api/v1/auth/**", "/signup", "/", "/login", "/Oauth2/**", "/auth/**", "/actuator/health", "/api/v1/oauth/**", "/api/oauth/**", "/env"
    };

    private final String[] GetPermitPatterns = {
            "/items/**"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(SwaggerPatterns)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, GetPermitPatterns).permitAll()
                .requestMatchers(PermitAllPatterns).permitAll()
                .anyRequest().authenticated()
        );

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(java.util.List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                SERVER_URL
        ));

        cfg.getAllowedOrigins().addAll(Arrays.asList(FRONT_URLS));

        cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","X-Requested-With"));
        cfg.setExposedHeaders(java.util.List.of("Location","Content-Disposition"));
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));

        cfg.setAllowCredentials(true);
        cfg.setMaxAge(1800L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }


}
