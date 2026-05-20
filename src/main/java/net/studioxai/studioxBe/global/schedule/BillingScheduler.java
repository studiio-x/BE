package net.studioxai.studioxBe.global.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.service.SubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void approveDailyBilling() {
        log.info("[BillingScheduler] daily billing started");

        subscriptionService.approveTodaySubscriptions();

        log.info("[BillingScheduler] daily billing finished");
    }
}
