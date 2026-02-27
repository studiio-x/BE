package net.studioxai.studioxBe.infra.ai.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.chat.entity.ChatMessage;
import net.studioxai.studioxBe.domain.chat.entity.enums.MessageRole;
import net.studioxai.studioxBe.infra.ai.dto.GeminiChatResult;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiGenerateRequest;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiChatClient {

    private final RestTemplate restTemplate;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;

    private static final String CONCEPT_PROMPT_TEMPLATE = """
            You are an AI image editing assistant for a product photography platform.
            The user has a current composite image that they want to modify.

            The first attached image is the current version the user is working on.
            If a mask image is provided (areas highlighted with color), focus modifications on those highlighted regions.
            If a reference image is provided, use it as style/mood inspiration.

            This is concept variation %d of 4. Each variation must be a distinctly different interpretation
            of the user's request. Be creative and offer meaningful variety in how you fulfill the request.

            Preserve the product's identity and key details. Output a high-resolution PNG.

            User's request: %s
            """;

    private static final String FINAL_IMAGE_PROMPT = """
            You are an AI image editing assistant. The user selected a concept design they liked.
            Now generate the final, polished, high-quality version of this concept.

            Refine the selected concept image with:
            - Higher detail and resolution
            - Perfect lighting and shadow consistency
            - Clean edges and seamless integration
            - Faithful to the original product details

            Original user request: %s

            Output a single high-resolution clean PNG.
            """;

    public List<String> generateConceptImages(
            String userPrompt,
            List<ChatMessage> contextMessages,
            String currentImageBase64,
            String referenceBase64,
            String maskBase64
    ) {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final int variation = i;
            futures.add(CompletableFuture.supplyAsync(() ->
                    generateSingleConcept(userPrompt, variation, currentImageBase64, referenceBase64, maskBase64)));
        }

        List<String> conceptBase64List = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            try {
                String base64 = future.get(120, TimeUnit.SECONDS);
                conceptBase64List.add(base64);
            } catch (Exception e) {
                log.error("Concept generation failed", e);
                throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
            }
        }

        return conceptBase64List;
    }

    public String generateFinalImage(
            String originalPrompt,
            String selectedConceptBase64
    ) {
        String prompt = String.format(FINAL_IMAGE_PROMPT, originalPrompt);

        List<GeminiGenerateRequest.Part> imageParts = new ArrayList<>();
        imageParts.add(imagePart(selectedConceptBase64));

        return callGeminiForImage(imageParts, prompt);
    }

    private String generateSingleConcept(
            String userPrompt,
            int variationIndex,
            String currentImageBase64,
            String referenceBase64,
            String maskBase64
    ) {
        String prompt = String.format(CONCEPT_PROMPT_TEMPLATE, variationIndex + 1, userPrompt);

        List<GeminiGenerateRequest.Part> imageParts = new ArrayList<>();
        imageParts.add(imagePart(currentImageBase64));
        if (maskBase64 != null) imageParts.add(imagePart(maskBase64));
        if (referenceBase64 != null) imageParts.add(imagePart(referenceBase64));

        return callGeminiForImage(imageParts, prompt);
    }

    private String callGeminiForImage(List<GeminiGenerateRequest.Part> imageParts, String prompt) {
        String url = props.baseUrl()
                + "/v1beta/models/"
                + props.model()
                + ":generateContent";

        GeminiGenerateRequest requestBody = GeminiGenerateRequest.of(prompt, imageParts);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", props.apiKey());

        HttpEntity<GeminiGenerateRequest> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        }

        String base64 = extractImageBase64(response.getBody());

        byte[] decoded = Base64.getDecoder().decode(base64);
        if (decoded.length == 0) {
            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        }

        return base64;
    }

    private GeminiGenerateRequest.Part imagePart(String base64Image) {
        return new GeminiGenerateRequest.Part(
                null,
                new GeminiGenerateRequest.InlineData("image/png", base64Image)
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

            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        } catch (AiExceptionHandler e) {
            throw e;
        } catch (Exception e) {
            throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
        }
    }
}
