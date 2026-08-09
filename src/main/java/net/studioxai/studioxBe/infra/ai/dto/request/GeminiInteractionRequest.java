package net.studioxai.studioxBe.infra.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiInteractionRequest {

    private String model;
    private List<InputItem> input;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputItem {
        private String type;     // "image", "video", "text"
        private String data;     // Base64 데이터 (이미지용)
        private String uri;      // Gemini File API URI (비디오/대용량 파일용)

        @JsonProperty("mime_type")
        private String mimeType; // e.g. "image/jpeg", "video/mp4"

        private String text;     // 텍스트 프롬프트

        public static InputItem image(String base64Data, String mimeType) {
            return InputItem.builder()
                    .type("image")
                    .data(base64Data)
                    .mimeType(mimeType)
                    .build();
        }

        public static InputItem videoUri(String fileUri, String mimeType) {
            return InputItem.builder()
                    .type("video")
                    .uri(fileUri)
                    .mimeType(mimeType)
                    .build();
        }

        public static InputItem text(String prompt) {
            return InputItem.builder()
                    .type("text")
                    .text(prompt)
                    .build();
        }
    }

    public static GeminiInteractionRequest of(String model, List<InputItem> inputItems) {
        return GeminiInteractionRequest.builder()
                .model(model)
                .input(inputItems)
                .build();
    }

    public static GeminiInteractionRequest of(String model, String base64Image, String mimeType, String prompt) {
        return GeminiInteractionRequest.builder()
                .model(model)
                .input(List.of(
                        InputItem.image(base64Image, mimeType),
                        InputItem.text(prompt)
                ))
                .build();
    }
}