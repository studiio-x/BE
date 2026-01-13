package net.studioxai.studioxBe.infra.ai.nanobanana;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.infra.ai.exception.AiErrorCode;
import net.studioxai.studioxBe.infra.ai.exception.AiExceptionHandler;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NanobananaClient {

    private final RestTemplate restTemplate;
    private final NanobananaProperties properties;

    public NanobananaGenerateResponse generateImage(String rawImageUrl, String templateImageUrl, String prompt) {
        NanobananaGenerateRequest request =
                new NanobananaGenerateRequest(
                        properties.model(),
                        rawImageUrl,
                        templateImageUrl,
                        prompt
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiKey());

        HttpEntity<NanobananaGenerateRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<NanobananaGenerateResponse> response =
                    restTemplate.exchange(
                            properties.baseUrl() + "/v1/images/generate",
                            HttpMethod.POST,
                            entity,
                            NanobananaGenerateResponse.class
                    );

            if (response.getBody() == null) {
                throw new AiExceptionHandler(AiErrorCode.AI_INVALID_RESPONSE);
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new AiExceptionHandler(AiErrorCode.AI_CALL_FAILED);
        }


    }
}
