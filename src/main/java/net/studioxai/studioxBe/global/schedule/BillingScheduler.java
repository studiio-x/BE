package net.studioxai.studioxBe.global.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.service.SubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    // TODO: 결제 실패 건 재시도 작성

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireCanceledSubscriptions() {
        subscriptionService.changeUserPlan();
    }
}
