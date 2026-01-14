package net.studioxai.studioxBe.infra.ai.nanobanana;

public record NanobananaGenerateRequest (
        String model,
        String cutoutImageUrl,
        String templateImageUrl,
        String prompt
){ }
