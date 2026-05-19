package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.response.ExchangeRateResponse;
import net.studioxai.studioxBe.domain.payment.entity.redis.ExchangeRate;
import net.studioxai.studioxBe.domain.payment.exception.ExchangeRateErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.ExchangeRateExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    @Value("${exchange-rate.api-key}")
    private String apiKey;

    private final ExchangeRateRepository exchangeRateRepository;

    private final RestClient restClient = RestClient.create("https://v6.exchangerate-api.com/v6");

    private static final String BASE_CURRENCY = "USD";
    private static final String TARGET_CURRENCY = "KRW";
    private static final String EXCHANGE_RATE_ID = BASE_CURRENCY + ":" + TARGET_CURRENCY;

    public void saveRate() {
        ExchangeRateResponse response = restClient.get()
                .uri("/{apiKey}/latest/{baseCurrency}", apiKey, BASE_CURRENCY)
                .retrieve()
                .body(ExchangeRateResponse.class);

        validateResponse(response);

        BigDecimal krwRate = response.conversionRates().get(TARGET_CURRENCY);

        if (krwRate == null) {
            throw new ExchangeRateExceptionHandler(ExchangeRateErrorCode.NOT_FOUND_KRW);
        }

        ExchangeRate exchangeRate = ExchangeRate.builder()
                .baseCurrency(response.baseCode())
                .targetCurrency(TARGET_CURRENCY)
                .rate(krwRate)
                .timeLastUpdateUtc(response.timeLastUpdateUtc())
                .timeNextUpdateUtc(response.timeNextUpdateUtc())
                .fetchedAt(LocalDateTime.now())
                .build();

        exchangeRateRepository.save(exchangeRate);

        log.info("[ExchangeRate] USD -> KRW 환율 Redis 저장 완료. rate={}", krwRate);
    }

    public BigDecimal getKrwRate() {
        ExchangeRate exchangeRate = exchangeRateRepository.findById(EXCHANGE_RATE_ID)
                .orElseThrow(() -> new ExchangeRateExceptionHandler(ExchangeRateErrorCode.NOT_FOUND_EXCHANGE_RATE));

        return exchangeRate.getRate();
    }

    private void validateResponse(ExchangeRateResponse response) {
        if (response == null) {
            throw new ExchangeRateExceptionHandler(ExchangeRateErrorCode.NOT_FOUND_RESPONSE);
        }

        if (!"success".equals(response.result())) {
            log.error("환율 API 호출에 실패했습니다. result=" + response.result());
            throw new ExchangeRateExceptionHandler(ExchangeRateErrorCode.INVALID_API_CALL);
        }

        if (response.conversionRates() == null) {
            throw new ExchangeRateExceptionHandler(ExchangeRateErrorCode.NOT_FOUND_CONVERSION_RATE);
        }
    }
}
