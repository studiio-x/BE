package net.studioxai.studioxBe.domain.payment.entity.redis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import lombok.Builder;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "exchangeRate", timeToLive = 90000)
public class ExchangeRate {
    @Id
    private String id; // 예: USD:KRW

    private String baseCurrency;

    private String targetCurrency;

    private BigDecimal rate;

    private String timeLastUpdateUtc;

    private String timeNextUpdateUtc;

    private LocalDateTime fetchedAt;

    @Builder
    public ExchangeRate(
            String baseCurrency,
            String targetCurrency,
            BigDecimal rate,
            String timeLastUpdateUtc,
            String timeNextUpdateUtc,
            LocalDateTime fetchedAt
    ) {
        this.id = baseCurrency + ":" + targetCurrency;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.timeLastUpdateUtc = timeLastUpdateUtc;
        this.timeNextUpdateUtc = timeNextUpdateUtc;
        this.fetchedAt = fetchedAt;
    }
}
