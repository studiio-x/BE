package net.studioxai.studioxBe.infra.ai.dto.request;

import java.util.List;


public record GeminiGenerateRequest(
        List<Content> contents
) {

    public static GeminiGenerateRequest of(
            String prompt,
            List<Part> imageParts
    ) {
        return new GeminiGenerateRequest(
                List.of(
                        new Content(
                                buildParts(prompt, imageParts)
                        )
                )
        );
    }

    private static List<Part> buildParts(
            String prompt,
            List<Part> imageParts
    ) {
        List<Part> parts = new java.util.ArrayList<>();
        parts.add(new Part(prompt, null));
        parts.addAll(imageParts);
        return parts;
    }

    public record Content(List<Part> parts) {}

    public record Part(
            String text,
            InlineData inline_data
    ) {}

    public record InlineData(
            String mime_type,
            String data
    ) {}
}



