package net.studioxai.studioxBe.global.schedule;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.service.ExchangeRateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void saveExchangeRate() {
        exchangeRateService.saveRate();
    }
}
