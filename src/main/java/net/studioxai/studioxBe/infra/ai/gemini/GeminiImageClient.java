package net.studioxai.studioxBe.infra.ai.gemini;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiGenerateRequest;
import net.studioxai.studioxBe.infra.ai.dto.response.GeminiGenerateResponse;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiImageClient {

    private final RestTemplate restTemplate;
    private final GeminiProperties properties;

    /* ======================
     * Public API
     * ====================== */

    /**
     * 배경 제거 (누끼)
     */
    public String removeBackground(String base64Image) {

        String prompt = """
        Remove the background completely from the image.

        Requirements:
        - Keep only the main subject
        - Preserve original shape and details
        - Background must be fully transparent
        - Do NOT add shadows
        - Do NOT add new objects
        - Output must be a clean PNG
        """;

        return generateImageInternal(
                List.of(imagePart(base64Image)),
                prompt
        );
    }

    /**
     * cutout + template 합성
     */
    public String generateCompositeImage(
            String cutoutBase64,
            String templateBase64,
            String prompt
    ) {
        return generateImageInternal(
                List.of(
                        imagePart(templateBase64),
                        imagePart(cutoutBase64)
                ),
                prompt
        );
    }

    private String generateImageInternal(List<GeminiGenerateRequest.Part> imageParts, String prompt) {
        GeminiGenerateRequest request =
                GeminiGenerateRequest.of(prompt, imageParts);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", properties.apiKey());

        HttpEntity<GeminiGenerateRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GeminiGenerateResponse> response =
                    restTemplate.postForEntity(
                            properties.baseUrl()
                                    + "/v1beta/models/"
                                    + properties.model()
                                    + ":generateContent",
                            entity,
                            GeminiGenerateResponse.class
                    );

            GeminiGenerateResponse body = response.getBody();
            if (body == null || body.candidates().isEmpty()) {
                throw new IllegalStateException("Empty Gemini response");
            }

            return extractBase64Image(body);

        } catch (Exception e) {
            log.error("[Gemini IMAGE ERROR]", e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    private GeminiGenerateRequest.Part imagePart(String base64Image) {
        return new GeminiGenerateRequest.Part(
                null,
                new GeminiGenerateRequest.InlineData(
                        "image/png",
                        base64Image
                )
        );
    }

    private String extractBase64Image(GeminiGenerateResponse response) {
        try {
            return response.candidates()
                    .get(0)
                    .content()
                    .parts()
                    .stream()
                    .filter(p -> p.inline_data() != null)
                    .findFirst()
                    .orElseThrow()
                    .inline_data()
                    .data();
        } catch (Exception e) {
            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        }
    }
}
