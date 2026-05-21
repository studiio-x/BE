package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.*;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
    private final BillingKeyRepository billingKeyRepository;

    private final BillingKeyApprovalService billingKeyApprovalService;
    private final UserService userService;

    private final List<SubscriptionStatus> activeStatuses = List.of(
            SubscriptionStatus.ACTIVE,
            SubscriptionStatus.CHANGE_SCHEDULED
    );
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public void changeSubscription(Long userId, Plan plan) throws IOException {
        User user = userService.getUserByIdOrThrow(userId);

        Subscription subscription = subscriptionRepository.findLatestByUser(user, activeStatuses)
                .orElseThrow(() -> new SubscriptionExceptionHandler(
                        SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND
                ));

        BigDecimal exchangeRate = exchangeRateService.getKrwRate();

        long amount = calculateChangeFee(exchangeRate, subscription, plan, LocalDateTime.now());

        if (amount > 0) {
           billingKeyApprovalService.chargeUpgradePlan(user, subscription, plan, amount);
        } else {
            subscription.cancelAtPeriodEnd("Change the Plan");

            BillingKey billingKey = billingKeyRepository.findByUser(user).orElseThrow(
                    () -> new BillingKeyExceptionHandler(BillingKeyErrorCode.NOT_FOUND_BILLING_KEY)
            );

            Subscription newSubscription = Subscription.downGradeSubscription(user, plan, billingKey, subscription);

            subscriptionRepository.save(newSubscription);
        }

    }

    @Transactional
    public void cancelSubscription(Long userId, String reason) {
        User user = userService.getUserByIdOrThrow(userId);

        Subscription subscription = subscriptionRepository.findLatestByUser(user, activeStatuses)
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

    @Transactional
    public void approveTodaySubscriptions() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        Long lastId = 0L;

        while (true) {
            List<Subscription> subscriptions = subscriptionRepository.findDailyBillingTargets(
                    startOfTomorrow,
                    now,
                    lastId,
                    activeStatuses,
                    PageRequest.of(0, BATCH_SIZE)
            );

            if (subscriptions.isEmpty()) {
                break;
            }

            log.info("[Billing] daily billing targets size={}", subscriptions.size());

            lastId = subscriptions.get(subscriptions.size() - 1).getId();

            for (Subscription subscription : subscriptions) {
                try {
                    billingKeyApprovalService.paySubscription(subscription);

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

    private long calculateChangeFee(BigDecimal krwRate, Subscription subscription, Plan plan, LocalDateTime now) {
        Plan currentPlan = subscription.getPlan();
        BigDecimal currentPrice = BigDecimal.valueOf(currentPlan.getPrice());
        BigDecimal newPrice = BigDecimal.valueOf(plan.getPrice());

        BigDecimal priceDiff = newPrice.subtract(currentPrice);

        if (priceDiff.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }
        else {
            long totalSeconds = Duration.between(
                    subscription.getCurrentPeriodStart(),
                    subscription.getCurrentPeriodEnd()
            ).getSeconds();


            long remainingSeconds = Duration.between(
                    now,
                    subscription.getCurrentPeriodEnd()
            ).getSeconds();

            if (totalSeconds <= 0 || remainingSeconds <= 0) {
                return 0L;
            }

            BigDecimal remainingRatio = BigDecimal.valueOf(remainingSeconds)
                    .divide(BigDecimal.valueOf(totalSeconds), 10, RoundingMode.HALF_UP);


            return priceDiff
                    .multiply(krwRate)
                    .multiply(remainingRatio)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
        }
    }

}
