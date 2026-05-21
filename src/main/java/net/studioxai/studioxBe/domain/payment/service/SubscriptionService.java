package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionExceptionHandler;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
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
    private final UserPlanRepository userPlanRepository;

    private final BillingKeyApprovalService billingKeyApprovalService;
    private final UserService userService;

    public void cancelSubscription(Long userId, String reason) {
        User user = userService.getUserByIdOrThrow(userId);
        Subscription subscription = subscriptionRepository.findLatestByUser(user)
                .orElseThrow(() -> new SubscriptionExceptionHandler(
                        SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND
                ));

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            return;
        }

        subscription.cancelAtPeriodEnd(reason);
    }

    public void changeUserPlan() {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> subscriptions =
                subscriptionRepository.findExpiredSubscriptions(
                        SubscriptionStatus.CANCEL_SCHEDULED,
                        now
                );

        for (Subscription subscription : subscriptions) {
            subscription.expire();

            UserPlan userPlan = userPlanRepository.findByUser(subscription.getUser())
                    .orElseThrow(() -> new UserPlanExceptionHandler(
                            UserPlanErrorCode.USER_PLAN_NOT_FOUNT
                    ));

            userPlan.changePlan(Plan.FREE);
        }
    }

    public void approveTodaySubscriptions() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        Long lastId = 0L;

        while (true) {
            List<Long> targetIds = subscriptionRepository.findDailyBillingTargetIds(
                    startOfTomorrow,
                    now,
                    lastId,
                    PageRequest.of(0, BATCH_SIZE)
            );

            if (targetIds.isEmpty()) {
                break;
            }

            log.info("[Billing] daily billing targets size={}", targetIds.size());

            lastId = targetIds.get(targetIds.size() - 1);

            for (Long subscriptionId : targetIds) {
                try {
                    billingKeyApprovalService.paySubscription(subscriptionId);
                } catch (Exception e) {
                    log.error(
                            "[Billing] failed to approve subscription. subscriptionId={}",
                            subscriptionId,
                            e
                    );
                }
            }
        }
    }

}
