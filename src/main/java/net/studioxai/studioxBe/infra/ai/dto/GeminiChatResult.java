package net.studioxai.studioxBe.infra.ai.dto;

import java.util.List;

public record GeminiChatResult(
        String textResponse,
        List<String> imageBase64List
) {
}
