package net.studioxai.studioxBe.infra.ai.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiGenerateRequest;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiInteractionRequest;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiOmniVideoClient {

    private static final String OMNI_MODEL = "gemini-omni-flash-preview";

    private final RestTemplate restTemplate;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;
    private final S3ImageUploader s3ImageUploader;

    public void generateAndUploadToS3(String base64Image, String prompt, String objectKey) {
        // 이미지 mimeType은 필요 시 확장 가능 (기본값: image/jpeg)
        byte[] videoBytes = generateVideoInternal(base64Image, "image/jpeg", prompt);

        try {
            s3ImageUploader.upload(objectKey, videoBytes, "video/mp4");
            log.info("S3 비디오 업로드 성공 - Key: {}, Size: {} bytes", objectKey, videoBytes.length);
        } catch (Exception e) {
            log.error("S3 비디오 업로드 실패 - Key: {}, Error: {}", objectKey, e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    private byte[] generateVideoInternal(String base64Image, String mimeType, String prompt) {
        String url = props.baseUrl() + "/v1beta/interactions?key=" + props.apiKey();

        GeminiInteractionRequest requestBody = GeminiInteractionRequest.of(
                OMNI_MODEL, base64Image, mimeType, prompt
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", props.apiKey());

        HttpEntity<GeminiInteractionRequest> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (HttpStatusCodeException e) {
            log.error("Gemini Interactions API 호출 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        } catch (Exception e) {
            log.error("Gemini Interactions API 호출 예외: {}", e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }

        return extractVideoBytes(response.getBody());
    }

    private byte[] extractVideoBytes(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Interactions API 응답 구조에서 video 데이터/URI 재귀 탐색
            byte[] videoBytes = findVideoData(root);
            if (videoBytes != null) {
                return videoBytes;
            }

            log.error("응답 내에서 비디오 데이터를 찾지 못했습니다. Response Body: {}", responseBody);
            throw new AiExceptionHandler(AiErrorCode.VIDEO_RESPONSE_INVALID);

        } catch (AiExceptionHandler e) {
            throw e;
        } catch (Exception e) {
            log.error("비디오 응답 파싱 실패: {}", e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.VIDEO_RESPONSE_INVALID);
        }
    }

    private byte[] findVideoData(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isObject()) {
            String type = node.path("type").asText("");
            String mimeType = node.path("mime_type").asText(node.path("mimeType").asText(""));

            // video 타입이거나 mime_type이 video/* 인 경우
            if ("video".equalsIgnoreCase(type) || mimeType.startsWith("video/")) {
                if (node.hasNonNull("data")) {
                    return Base64.getDecoder().decode(node.path("data").asText());
                }
                if (node.hasNonNull("uri")) {
                    return downloadVideoFromUri(node.path("uri").asText());
                }
                if (node.hasNonNull("fileUri")) {
                    return downloadVideoFromUri(node.path("fileUri").asText());
                }
            }

            // 하위 필드 재귀 탐색 (steps, outputs, content 등)
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                byte[] result = findVideoData(fields.next().getValue());
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                byte[] result = findVideoData(item);
                if (result != null) return result;
            }
        }

        return null;
    }

    private byte[] downloadVideoFromUri(String fileUri) {
        try {
            String downloadUrl = fileUri.contains("?")
                    ? fileUri + "&key=" + props.apiKey()
                    : fileUri + "?key=" + props.apiKey();

            ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
            if (response.getBody() == null || response.getBody().length == 0) {
                throw new AiExceptionHandler(AiErrorCode.VIDEO_RESPONSE_INVALID);
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini File URI 영상 다운로드 실패 - URI: {}, error: {}", fileUri, e.getMessage());
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }
}