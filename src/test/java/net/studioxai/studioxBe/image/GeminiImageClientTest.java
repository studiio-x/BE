package net.studioxai.studioxBe.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiImageClient;
import net.studioxai.studioxBe.infra.ai.gemini.GeminiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiImageClientTest {

    @Mock
    private RestTemplate restTemplate;

    private GeminiImageClient geminiImageClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        GeminiProperties props = new GeminiProperties(
                "https://generativelanguage.googleapis.com",
                "test-api-key",
                "gemini-2.5-flash-image"
        );
        geminiImageClient = new GeminiImageClient(restTemplate, props, objectMapper);
    }

    @Test
    @DisplayName("누끼따기 성공 - Gemini 정상 응답 시 base64 반환")
    void removeBackground_success() {
        String inputBase64 = Base64.getEncoder().encodeToString("test-image".getBytes());
        String outputBase64 = Base64.getEncoder().encodeToString("cutout-image".getBytes());

        String geminiResponse = """
                {
                    "candidates": [{
                        "content": {
                            "parts": [{
                                "inlineData": {
                                    "mimeType": "image/png",
                                    "data": "%s"
                                }
                            }]
                        }
                    }]
                }
                """.formatted(outputBase64);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse));

        String result = geminiImageClient.removeBackground(inputBase64);

        assertThat(result).isEqualTo(outputBase64);
    }

    @Test
    @DisplayName("Gemini API 4xx 에러 시 AiExceptionHandler 발생 (기존 500 -> 502)")
    void removeBackground_gemini4xxError_throwsAiException() {
        String inputBase64 = Base64.getEncoder().encodeToString("test-image".getBytes());

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));

        assertThatThrownBy(() -> geminiImageClient.removeBackground(inputBase64))
                .isInstanceOf(AiExceptionHandler.class);
    }

    @Test
    @DisplayName("Gemini API 5xx 에러 시 AiExceptionHandler 발생 (기존 500 -> 502)")
    void removeBackground_gemini5xxError_throwsAiException() {
        String inputBase64 = Base64.getEncoder().encodeToString("test-image".getBytes());

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini server error"));

        assertThatThrownBy(() -> geminiImageClient.removeBackground(inputBase64))
                .isInstanceOf(AiExceptionHandler.class);
    }

    @Test
    @DisplayName("네트워크 오류 시 AiExceptionHandler 발생 (기존 500 -> 502)")
    void removeBackground_networkError_throwsAiException() {
        String inputBase64 = Base64.getEncoder().encodeToString("test-image".getBytes());

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> geminiImageClient.removeBackground(inputBase64))
                .isInstanceOf(AiExceptionHandler.class);
    }

    @Test
    @DisplayName("Gemini 응답에 이미지 데이터 없으면 ImageExceptionHandler 발생")
    void removeBackground_noImageInResponse_throwsImageException() {
        String inputBase64 = Base64.getEncoder().encodeToString("test-image".getBytes());

        String geminiResponse = """
                {
                    "candidates": [{
                        "content": {
                            "parts": [{
                                "text": "I cannot process this image"
                            }]
                        }
                    }]
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse));

        assertThatThrownBy(() -> geminiImageClient.removeBackground(inputBase64))
                .isInstanceOf(ImageExceptionHandler.class);
    }
}
