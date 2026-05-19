package net.studioxai.studioxBe.domain.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.exception.TossPaymentErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.TossPaymentExceptionHandler;
import net.studioxai.studioxBe.domain.payment.util.JsonUtil;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TossService {
    @Value("${toss.pay.base-url}")
    private String baseUrl;

    @Value("${toss.pay.secret-key}")
    private String tossSecretKey;

    @Value("${toss.pay.secure-key}")
    private String tossSecureKey;

    @Value("${toss.pay.client-key}")
    private String tossClientKey;

    private final JsonUtil jsonUtil;

    public <T> T getResponse(Object dto, Class<T> dtoClass, String uriPath) throws IOException {
        JSONObject requestJsonObject = jsonUtil.toJSONObject(dto);
        JSONObject responseJsonObject= sendRequest(requestJsonObject, uriPath);
        return jsonUtil.toDto(responseJsonObject, dtoClass);
    }

    public JSONObject sendRequest(JSONObject requestData, String uriPath) throws IOException {
        try {
            String requestUrl = baseUrl + uriPath;
            HttpURLConnection connection = createConnection(tossSecretKey, requestUrl);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            boolean isSuccess = responseCode >= 200 && responseCode < 300;

            InputStream responseStream = isSuccess
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (responseStream == null) {
                throw new TossPaymentExceptionHandler(TossPaymentErrorCode.TOSS_EMPTY_RESPONSE);
            }

            try (Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
                JSONObject response = (JSONObject) new JSONParser().parse(reader);

                if (!isSuccess) {
                    log.warn(
                            "토스페이먼츠 요청 실패. status={}, response={}",
                            responseCode,
                            response
                    );

                    throw new TossPaymentExceptionHandler(TossPaymentErrorCode.TOSS_REQUEST_FAILED);
                }

                return response;
            }

        } catch (TossPaymentExceptionHandler e) {
            throw e;

        } catch (ParseException e) {
            log.error("토스페이먼츠 응답 파싱 실패", e);
            throw new TossPaymentExceptionHandler(TossPaymentErrorCode.TOSS_RESPONSE_PARSE_FAILED);

        } catch (IOException e) {
            log.error("토스페이먼츠 통신 실패", e);
            throw new TossPaymentExceptionHandler(TossPaymentErrorCode.TOSS_REQUEST_FAILED);
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException, MalformedURLException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

}
