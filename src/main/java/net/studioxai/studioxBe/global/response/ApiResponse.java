package net.studioxai.studioxBe.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    @JsonProperty("status")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @Builder
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> onSuccess(int code) {
        return ApiResponse.<T>builder().code(code).message("요청에 성공하였습니다.").data(null).build();
    }

    public static <T> ApiResponse<T> onSuccess(int code, T data) {
        return ApiResponse.<T>builder().code(code).message("요청에 성공하였습니다.").data(data).build();
    }
}