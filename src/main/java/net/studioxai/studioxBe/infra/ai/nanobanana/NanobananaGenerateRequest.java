package net.studioxai.studioxBe.infra.ai.nanobanana;

public record NanobananaGenerateRequest (
        String model,
        String rawImageUrl,
        String templateImageUrl,
        String prompt
){ }
