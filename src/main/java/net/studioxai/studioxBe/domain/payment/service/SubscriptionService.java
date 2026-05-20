package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final int BATCH_SIZE = 100;

    private final SubscriptionRepository subscriptionRepository;
    private final BillingKeyApprovalService billingKeyApprovalService;

    public void approveTodaySubscriptions() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        while (true) {
            List<Subscription> targets = subscriptionRepository.findDailyBillingTargets(
                    startOfTomorrow,
                    PageRequest.of(0, BATCH_SIZE)
            );

            if (targets.isEmpty()) {
                break;
            }

            log.info("[Billing] daily billing targets size={}", targets.size());

            for (Subscription subscription : targets) {
                try {
                    billingKeyApprovalService.paySubscription(subscription.getId());
                } catch (Exception e) {
                    log.error(
                            "[Billing] failed to approve subscription. subscriptionId={}",
                            subscription.getId(),
                            e
                    );
                }
            }
        }
    }

}
