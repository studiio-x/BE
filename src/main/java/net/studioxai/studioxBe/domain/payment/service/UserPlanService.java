package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.ExtraCreditSummaryDto;
import net.studioxai.studioxBe.domain.payment.dto.response.MyPlanResponse;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionExceptionHandler;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.ExtraCreditRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserPlanService {
    private final UserService userService;

    private final UserPlanRepository userPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ExtraCreditRepository extraCreditRepository;

    public MyPlanResponse getUserPlan(Long userId) {
        User user = userService.getUserByIdOrThrow(userId);

        UserPlan userPlan = userPlanRepository.findByUser(user).orElseThrow(
                () -> new UserPlanExceptionHandler(UserPlanErrorCode.USER_PLAN_NOT_FOUNT)
        );

        List<SubscriptionStatus> activeStatuses = List.of(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.CHANGE_SCHEDULED
        );

        Subscription subscription = subscriptionRepository.findLatestByUser(user, activeStatuses).orElseThrow(
                () -> new SubscriptionExceptionHandler(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND)
        );

        ExtraCreditSummaryDto extraCreditSummary = extraCreditRepository.findAvailableCreditSummary(userId, LocalDateTime.now());

        return toMyPlanResponse(userPlan, subscription, extraCreditSummary);
    }

    private MyPlanResponse toMyPlanResponse(UserPlan userPlan, Subscription subscription, ExtraCreditSummaryDto extraCreditSummary) {
        int totalCredit = userPlan.getPlan().getCredit() + extraCreditSummary.totalCreditAmount() - userPlan.getCredit();

        return new MyPlanResponse(
                totalCredit,
                userPlan.getPlan().getCredit() - userPlan.getCredit(),
                extraCreditSummary.totalCreditAmount(),
                extraCreditSummary.nearestExpiredAt(),
                subscription.getPlan().getPrice(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                userPlan.getPlan()
        );

    }
}
