package net.studioxai.studioxBe.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EnvironmentUtil {
    private final Environment environment;

    private final String BLUE = "blue";
    private final String GREEN = "green";
    private final String LOCAL = "local";

    private final List<String> PROD = List.of("blue", "green", "prod");

    public Boolean isBlueProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        List<String> currentProfile = Arrays.stream(activeProfiles).toList();
        return currentProfile.contains(BLUE);
    }

    public Boolean isGreenProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        List<String> currentProfile = Arrays.stream(activeProfiles).toList();
        return currentProfile.contains(GREEN);
    }

    public Boolean isLocalProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        List<String> currentProfile = Arrays.stream(activeProfiles).toList();
        return currentProfile.contains(LOCAL);
    }

    public Boolean isProdProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        List<String> currentProfile = Arrays.stream(activeProfiles).toList();
        return CollectionUtils.containsAny(PROD, currentProfile);
    }

}
