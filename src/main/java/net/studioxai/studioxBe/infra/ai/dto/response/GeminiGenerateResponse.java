package net.studioxai.studioxBe.infra.ai.dto.response;

import java.util.List;

public record GeminiGenerateResponse(
        List<Candidate> candidates
) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(
            InlineData inline_data
    ) {}

    public record InlineData(
            String mime_type,
            String data
    ) {}
}


