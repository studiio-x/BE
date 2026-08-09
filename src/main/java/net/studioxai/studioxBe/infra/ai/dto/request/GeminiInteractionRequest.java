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
        private String type; // "image" 또는 "text"
        private String data; // Base64 이미지 데이터

        @JsonProperty("mime_type")
        private String mimeType; // e.g. "image/jpeg"

        private String text; // 텍스트 프롬프트

        public static InputItem image(String base64Data, String mimeType) {
            return InputItem.builder()
                    .type("image")
                    .data(base64Data)
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