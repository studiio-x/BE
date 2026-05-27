package net.studioxai.studioxBe.domain.payment.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public JSONObject toJSONObject(Object dto) {
        Map<String, Object> map = objectMapper.convertValue(
                dto,
                new TypeReference<Map<String, Object>>() {}
        );

        return new JSONObject(map);
    }

    public <T> T toDto(JSONObject jsonObject, Class<T> dtoClass) {
        return objectMapper.convertValue(jsonObject, dtoClass);
    }
}
