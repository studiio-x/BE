package net.studioxai.studioxBe.global.response;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final String[] ESCAPE_PATTERNS = {
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/webjars"
    };

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        String requestUri = request.getURI().getPath();
        for (String pattern : ESCAPE_PATTERNS) {
            if (requestUri.startsWith(pattern)) {
                return body;
            }
        }

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();

        if (HttpStatus.resolve(statusCode) != null && HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
            String method = ((ServletServerHttpRequest) request).getServletRequest().getMethod();
            return ApiResponse.onSuccess(statusProvider(method), body);
        }

        return body;
    }

    private int statusProvider(String method) {
        return switch (method) {
            case "POST" -> 201;
            case "DELETE" -> 204;
            default -> 200;
        };
    }
}