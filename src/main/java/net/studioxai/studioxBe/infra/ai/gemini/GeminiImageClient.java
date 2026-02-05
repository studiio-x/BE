package net.studioxai.studioxBe.infra.ai.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiGenerateRequest;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiImageClient {

    private final RestTemplate restTemplate;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;

    /* ======================
     * Public API
     * ====================== */

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

        return generateImageInternal(List.of(imagePart(base64Image)), prompt);
    }

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

    private String generateImageInternal(
            List<GeminiGenerateRequest.Part> imageParts,
            String prompt
    ) {

        String url = props.baseUrl()
                + "/v1beta/models/"
                + props.model()
                + ":generateContent";

        GeminiGenerateRequest requestBody =
                GeminiGenerateRequest.of(prompt, imageParts);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", props.apiKey());

        HttpEntity<GeminiGenerateRequest> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        }

        String base64 = extractImageBase64(response.getBody());

        byte[] decoded = Base64.getDecoder().decode(base64);

        if (decoded.length == 0) {
            throw new ImageExceptionHandler(ImageErrorCode.GEMINI_RESPONSE_INVALID);
        }

        return base64;
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

    private String extractImageBase64(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode parts = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts");

            for (JsonNode part : parts) {
                JsonNode inlineData = part.path("inlineData");
                if (!inlineData.isMissingNode()) {
                    String base64 = inlineData.path("data").asText();
                    if (!base64.isBlank()) {
                        return base64;
                    }
                }
            }

            throw new ImageExceptionHandler(ImageErrorCode.GEMINI_RESPONSE_INVALID);

        } catch (Exception e) {
            throw new ImageExceptionHandler(ImageErrorCode.GEMINI_RESPONSE_INVALID);
        }
    }
}
