package net.studioxai.studioxBe.payment;

import com.sun.net.httpserver.HttpServer;
import net.studioxai.studioxBe.domain.payment.exception.TossPaymentExceptionHandler;
import net.studioxai.studioxBe.domain.payment.service.TossService;
import net.studioxai.studioxBe.domain.payment.util.JsonUtil;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TossServiceTest {

    @Mock
    private JsonUtil jsonUtil;

    private TossService tossService;

    private HttpServer server;

    private String baseUrl;

    private static final String TOSS_SECRET_KEY = "test_sk_123";

    @BeforeEach
    void setUp() throws IOException {
        tossService = new TossService(jsonUtil);

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();

        baseUrl = "http://localhost:" + server.getAddress().getPort();

        ReflectionTestUtils.setField(tossService, "baseUrl", baseUrl);
        ReflectionTestUtils.setField(tossService, "tossSecretKey", TOSS_SECRET_KEY);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("sendRequest 성공 - Toss 응답 JSON을 반환한다")
    void sendRequest_success() throws IOException {
        // given
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        AtomicReference<String> contentTypeHeader = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();

        registerResponse(
                "/v1/test",
                200,
                "{\"billingKey\":\"billing-key-123\",\"method\":\"카드\"}",
                authorizationHeader,
                contentTypeHeader,
                requestBody
        );

        JSONObject requestData = new JSONObject();
        requestData.put("customerKey", "customer-key");

        String expectedAuthorization = "Basic " + Base64.getEncoder()
                .encodeToString((TOSS_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

        // when
        JSONObject response = tossService.sendRequest(requestData, "/v1/test");

        // then
        assertThat(response.get("billingKey")).isEqualTo("billing-key-123");
        assertThat(response.get("method")).isEqualTo("카드");

        assertThat(authorizationHeader.get()).isEqualTo(expectedAuthorization);
        assertThat(contentTypeHeader.get()).contains("application/json");
        assertThat(requestBody.get()).contains("\"customerKey\":\"customer-key\"");
    }

    @Test
    @DisplayName("sendRequest 실패 - Toss가 4xx 응답을 반환하면 예외가 발생한다")
    void sendRequest_fail_whenTossReturns4xx() {
        // given
        registerResponse(
                "/v1/fail",
                400,
                "{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청입니다.\"}",
                new AtomicReference<>(),
                new AtomicReference<>(),
                new AtomicReference<>()
        );

        JSONObject requestData = new JSONObject();
        requestData.put("customerKey", "customer-key");

        // when & then
        assertThatThrownBy(() -> tossService.sendRequest(requestData, "/v1/fail"))
                .isInstanceOf(TossPaymentExceptionHandler.class);
    }

    @Test
    @DisplayName("sendRequest 실패 - Toss 응답이 JSON 형식이 아니면 예외가 발생한다")
    void sendRequest_fail_whenResponseIsInvalidJson() {
        // given
        registerResponse(
                "/v1/invalid-json",
                200,
                "this-is-not-json",
                new AtomicReference<>(),
                new AtomicReference<>(),
                new AtomicReference<>()
        );

        JSONObject requestData = new JSONObject();
        requestData.put("customerKey", "customer-key");

        // when & then
        assertThatThrownBy(() -> tossService.sendRequest(requestData, "/v1/invalid-json"))
                .isInstanceOf(TossPaymentExceptionHandler.class);
    }

    @Test
    @DisplayName("getResponse 성공 - DTO를 JSON으로 변환하고 응답 JSON을 DTO로 변환한다")
    void getResponse_success() throws IOException {
        // given
        TestRequest requestDto = new TestRequest("customer-key");

        JSONObject requestJson = new JSONObject();
        requestJson.put("customerKey", "customer-key");

        TestResponse expectedResponse = new TestResponse("billing-key-123");

        given(jsonUtil.toJSONObject(requestDto))
                .willReturn(requestJson);

        given(jsonUtil.toDto(any(JSONObject.class), eq(TestResponse.class)))
                .willReturn(expectedResponse);

        registerResponse(
                "/v1/get-response-test",
                200,
                "{\"billingKey\":\"billing-key-123\"}",
                new AtomicReference<>(),
                new AtomicReference<>(),
                new AtomicReference<>()
        );

        // when
        TestResponse result = tossService.getResponse(
                requestDto,
                TestResponse.class,
                "/v1/get-response-test"
        );

        // then
        assertThat(result).isEqualTo(expectedResponse);

        verify(jsonUtil).toJSONObject(requestDto);

        ArgumentCaptor<JSONObject> responseCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(jsonUtil).toDto(responseCaptor.capture(), eq(TestResponse.class));

        JSONObject capturedResponseJson = responseCaptor.getValue();
        assertThat(capturedResponseJson.get("billingKey")).isEqualTo("billing-key-123");
    }

    private void registerResponse(
            String path,
            int statusCode,
            String responseBody,
            AtomicReference<String> authorizationHeader,
            AtomicReference<String> contentTypeHeader,
            AtomicReference<String> requestBody
    ) {
        server.createContext(path, exchange -> {
            authorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            contentTypeHeader.set(exchange.getRequestHeaders().getFirst("Content-Type"));

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requestBody.set(body);

            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
    }

    private record TestRequest(String customerKey) {
    }

    private record TestResponse(String billingKey) {
    }
}