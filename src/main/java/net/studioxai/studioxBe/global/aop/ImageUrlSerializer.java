package net.studioxai.studioxBe.global.aop;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageUrlSerializer extends JsonSerializer<Object> {
    @Value("${server.image-domain}")
    private String imageDomain;

    @Override
    public void serialize(
            Object value,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
            return;
        }

        if (value instanceof String str) {
            jsonGenerator.writeString(convert(str));
            return;
        }


        if (value instanceof List<?> list) {
            jsonGenerator.writeStartArray();
            for (Object item : list) {
                if (item instanceof String strItem) {
                    jsonGenerator.writeString(convert(strItem));
                } else {
                    jsonGenerator.writeNull();
                }
            }
            jsonGenerator.writeEndArray();
        }

    }

    private String convert(String image) {
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        return imageDomain + image;
    }
}
