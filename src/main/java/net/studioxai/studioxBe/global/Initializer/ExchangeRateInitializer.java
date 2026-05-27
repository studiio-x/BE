package net.studioxai.studioxBe.global.Initializer;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.service.ExchangeRateService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeRateInitializer {

    private final ExchangeRateService exchangeRateService;

    @EventListener(ApplicationReadyEvent.class)
    public void initExchangeRate() {
        exchangeRateService.saveRate();
    }
}