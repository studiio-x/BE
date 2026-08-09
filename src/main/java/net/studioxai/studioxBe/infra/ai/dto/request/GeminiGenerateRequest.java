package net.studioxai.studioxBe.infra.ai.dto.request;

import java.util.ArrayList;
import java.util.List;


public record GeminiGenerateRequest(
        List<Content> contents,
        GenerationConfig generationConfig
        ) {

    public static GeminiGenerateRequest ofImage(
            String prompt,
            List<Part> imageParts
    ) {
        return new GeminiGenerateRequest(
                List.of(
                        new Content(
                                buildParts(prompt, imageParts)
                        )
                ),
                new GenerationConfig(List.of("IMAGE"))
        );
    }

    public static GeminiGenerateRequest ofVideo(
            String prompt,
            List<Part> inputParts
    ) {
        return new GeminiGenerateRequest(
                List.of(new Content(buildParts(prompt, inputParts))),
                new GenerationConfig(List.of("video")) // 비디오 생성 modality
        );
    }

    private static List<Part> buildParts(
            String prompt,
            List<Part> imageParts
    ) {
        List<Part> parts = new ArrayList<>();
        if (prompt != null && !prompt.isBlank()) {
            parts.add(Part.fromText(prompt));
        }
        if (imageParts != null) {
            parts.addAll(imageParts);
        }
        return parts;
    }

    public record Content(List<Part> parts) {}

    public record Part(
            String text,
            InlineData inline_data
                ) {
        public static Part fromText(String text) {
            return new Part(text, null);
        }

        public static Part fromImageBase64(String base64Image, String mimeType) {
            return new Part(null, new InlineData(mimeType, base64Image));
        }

    }

    public record InlineData(
            String mime_type,
            String data
    ) {}

    public record GenerationConfig(
            List<String> responseModalities
    ) {}
}



