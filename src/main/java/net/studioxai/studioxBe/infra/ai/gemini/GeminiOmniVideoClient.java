package net.studioxai.studioxBe.infra.ai.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiGenerateRequest;
import net.studioxai.studioxBe.infra.ai.dto.request.GeminiInteractionRequest;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import net.studioxai.studioxBe.infra.s3.S3ImageLoader;
import net.studioxai.studioxBe.infra.s3.S3ImageUploader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiOmniVideoClient {

    private static final String OMNI_MODEL = "gemini-omni-flash-preview";
    private static final String VIDEO_REFINE_PROMPT_TEMPLATE =
            "Modify and refine the provided video according to the following request while keeping the video context intact. User request: %s";

    private final RestTemplate restTemplate;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;
    private final S3ImageUploader s3ImageUploader;
    private final S3ImageLoader s3ImageLoader;

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

    public void refineVideoFromS3Url(
            String inputVideoObjectKey,
            String userPrompt,
            String targetObjectKey
    ) {
        // 1. S3에서 원본 비디오 데이터(byte[]) 로드
        byte[] inputVideoBytes = s3ImageLoader.loadAsBytes(inputVideoObjectKey);

        // 2. Gemini File API에 업로드하여 fileUri 획득
        String geminiFileUri = uploadVideoToGeminiFileApi(inputVideoBytes, "video/mp4");

        // 3. Interactions API 요청 바디 구성 (비디오 URI + 수정 프롬프트)
        List<GeminiInteractionRequest.InputItem> inputItems = new ArrayList<>();

        // 원본 비디오 참조 전달
        inputItems.add(GeminiInteractionRequest.InputItem.videoUri(geminiFileUri, "video/mp4"));

        // 프롬프트 전달
        String formattedPrompt = String.format(VIDEO_REFINE_PROMPT_TEMPLATE, userPrompt);
        inputItems.add(GeminiInteractionRequest.InputItem.text(formattedPrompt));

        // 4. Gemini Interactions API 호출하여 수정된 비디오 수신
        byte[] outputVideoBytes = callInteractionsApi(inputItems);

        // 5. 결과 비디오 S3 업로드
        try {
            s3ImageUploader.upload(targetObjectKey, outputVideoBytes, "video/mp4");
            log.info("수정된 비디오 S3 업로드 완료 - Key: {}, Size: {} bytes", targetObjectKey, outputVideoBytes.length);
        } catch (Exception e) {
            log.error("수정된 비디오 S3 업로드 실패 - Key: {}, Error: {}", targetObjectKey, e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    private String uploadVideoToGeminiFileApi(byte[] videoBytes, String mimeType) {
        String uploadUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + props.apiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(videoBytes.length);
        headers.set("X-Goog-Upload-Protocol", "raw");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(videoBytes, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode fileNode = root.path("file");

            String fileName = fileNode.path("name").asText(); // 예: "files/h5z01lgmp1zi"
            String fileUri = fileNode.path("uri").asText();   // 예: "https://generativelanguage.googleapis.com/v1beta/files/h5z01lgmp1zi"

            if (fileUri.isBlank() || fileName.isBlank()) {
                throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
            }

            // 파일 상태가 ACTIVE가 될 때까지 폴링 대기
            waitForFileActive(fileName);

            log.info("Gemini File API 비디오 업로드 및 ACTIVE 처리 완료 - File URI: {}", fileUri);
            return fileUri;
        } catch (Exception e) {
            log.error("Gemini File API 업로드 및 대기 실패: {}", e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    private void waitForFileActive(String fileName) {
        String getFileUrl = props.baseUrl() + "/v1beta/" + fileName + "?key=" + props.apiKey();

        int maxRetries = 30;       // 최대 30회 시도 (최대 30초 대기)
        int pollIntervalMs = 1000;  // 1초 간격 조회

        for (int i = 0; i < maxRetries; i++) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(getFileUrl, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());
                String state = root.path("state").asText();

                if ("ACTIVE".equalsIgnoreCase(state)) {
                    log.info("Gemini 비디오 파일 준비 완료 - State: ACTIVE ({}/{}회 시도)", i + 1, maxRetries);
                    return;
                } else if ("FAILED".equalsIgnoreCase(state)) {
                    log.error("Gemini 비디오 처리 실패 - State: FAILED");
                    throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
                }

                log.info("Gemini 비디오 인코딩 중... 현재 상태: {} ({}/{}회 시도)", state, i + 1, maxRetries);
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
            } catch (Exception e) {
                log.warn("Gemini File 상태 조회 중 예외 발생 (재시도 중): {}", e.getMessage());
                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
    }

    private byte[] callInteractionsApi(List<GeminiInteractionRequest.InputItem> inputItems) {
        String url = props.baseUrl() + "/v1beta/interactions?key=" + props.apiKey();

        GeminiInteractionRequest requestBody = GeminiInteractionRequest.of(OMNI_MODEL, inputItems);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", props.apiKey());

        HttpEntity<GeminiInteractionRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return extractVideoBytes(response.getBody());
        } catch (HttpStatusCodeException e) {
            log.error("Gemini Interactions API 호출 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        } catch (Exception e) {
            log.error("Gemini Interactions API 호출 예외: {}", e.getMessage(), e);
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }
    }

    private byte[] generateVideoInternal(List<GeminiInteractionRequest.InputItem> inputItems) {
        String url = props.baseUrl() + "/v1beta/interactions?key=" + props.apiKey();

        GeminiInteractionRequest requestBody = GeminiInteractionRequest.of(OMNI_MODEL, inputItems);

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
}