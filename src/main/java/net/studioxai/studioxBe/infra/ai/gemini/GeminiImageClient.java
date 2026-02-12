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

    String CUTOUT_PROMPT = """
            Remove the background completely from the image.

            Requirements:
            - Keep only the main subject
            - Preserve original shape and details
            - Background must be fully transparent
            - Do NOT add shadows
            - Do NOT add new objects
            - Output must be a clean PNG
        """;

    private static final String COMPOSITE_PROMPT = """
        Naturally composite the provided cutout product image into the template background.
        
        Requirements:
        - Place the cutout subject realistically within the template
        - Match perspective, scale, and alignment
        - Preserve original colors and details
        - Do NOT add new objects
        - Do NOT alter the template background
        - Output must be a clean PNG
            
        Key Refinements:
        - Lighting & Shadow: Generate realistic soft shadows beneath and behind the product that match the template's light source.
        - Global Illumination: Ensure the product reflects the ambient colors and tones of the background for a cohesive look.
        - Seamless Integration: Blend the contact points naturally so the product appears to be sitting "in" the fabric/surface, not just floating on top.
        - Perspective & Scale: Maintain perfect 3D perspective and relative scale consistent with the template.
        - Preservation: Keep the product's original logo and essential details intact.
        - Quality: Output a high-resolution, clean PNG with no artifacts.
    """;

    public String removeBackground(String base64Image) {
        return generateImageInternal(List.of(imagePart(base64Image)), CUTOUT_PROMPT);
    }

    public String generateCompositeImage(String cutoutBase64, String templateBase64) {
        return generateImageInternal(
                List.of(
                        imagePart(templateBase64),
                        imagePart(cutoutBase64)
                ),
                COMPOSITE_PROMPT
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
