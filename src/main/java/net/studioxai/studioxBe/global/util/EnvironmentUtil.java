package net.studioxai.studioxBe.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EnvironmentUtil {
    private final Environment environment;

    private static final String BLUE = "blue";
    private static final String GREEN = "green";
    private static final String LOCAL = "local";

    private static final Set<String> PROD_PROFILES = Set.of("blue", "green", "prod");

    public boolean isBlueProfile() {
        return hasProfile(BLUE);
    }

    public boolean isGreenProfile() {
        return hasProfile(GREEN);
    }

    public boolean isLocalProfile() {
        return hasProfile(LOCAL);
    }

    public boolean isProdProfile() {
        return hasAnyProfile(PROD_PROFILES);
    }

    private Set<String> activeProfiles() {
        return Set.of(environment.getActiveProfiles());
    }

    private boolean hasProfile(String profile) {
        return activeProfiles().contains(profile);
    }

    private boolean hasAnyProfile(Set<String> profiles) {
        return activeProfiles().stream().anyMatch(profiles::contains);
    }
}
