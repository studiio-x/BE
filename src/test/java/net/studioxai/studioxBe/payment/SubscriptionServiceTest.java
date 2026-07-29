package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyApprovalService;
import net.studioxai.studioxBe.domain.payment.service.ExchangeRateService;
import net.studioxai.studioxBe.domain.payment.service.SubscriptionService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private BillingKeyRepository billingKeyRepository;

    @Mock
    private BillingKeyApprovalService billingKeyApprovalService;

    @Mock
    private UserService userService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("구독 변경 금액 조회 - 업그레이드이면 남은 기간 비율에 따라 양수 금액을 반환한다")
    void getSubscriptionPrice_upgrade_returnsPositiveAmount() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);
        Subscription subscription = mock(Subscription.class);

        Plan currentPlan = cheapestPlan();
        Plan targetPlan = mostExpensivePlan();

        assertThat(targetPlan.getPrice()).isGreaterThan(currentPlan.getPrice());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(15);
        LocalDateTime end = now.plusDays(15);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(userPlan.getPlan()).thenReturn(currentPlan);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(subscription));

        when(subscription.getCurrentPeriodStart()).thenReturn(start);
        when(subscription.getCurrentPeriodEnd()).thenReturn(end);

        // when
        long amount = subscriptionService.getSubscriptionPrice(userId, targetPlan);

        // then
        assertThat(amount).isPositive();
    }

    @Test
    @DisplayName("구독 변경 금액 조회 - 다운그레이드이면 0원을 반환한다")
    void getSubscriptionPrice_downgrade_returnsZero() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);
        Subscription subscription = mock(Subscription.class);

        Plan currentPlan = mostExpensivePlan();
        Plan targetPlan = cheapestPlan();

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(userPlan.getPlan()).thenReturn(currentPlan);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(subscription));

        // when
        long amount = subscriptionService.getSubscriptionPrice(userId, targetPlan);

        // then
        assertThat(amount).isZero();
    }

    @Test
    @DisplayName("구독 변경 - 업그레이드이면 추가 결제를 요청한다")
    void changeSubscription_upgrade_chargesUpgradeFee() throws IOException {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);
        Subscription subscription = mock(Subscription.class);

        Plan currentPlan = cheapestPlan();
        Plan targetPlan = mostExpensivePlan();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(10);
        LocalDateTime end = now.plusDays(20);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(subscription));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(userPlan.getPlan()).thenReturn(currentPlan);

        when(subscription.getCurrentPeriodStart()).thenReturn(start);
        when(subscription.getCurrentPeriodEnd()).thenReturn(end);

        // when
        subscriptionService.changeSubscription(userId, targetPlan);

        // then
        verify(billingKeyApprovalService).chargeUpgradePlan(
                eq(user),
                eq(subscription),
                eq(targetPlan),
                longThat(amount -> amount > 0)
        );

        verify(subscription, never()).cancelAtPeriodEnd(anyString());
        verify(billingKeyRepository, never()).findByUser(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("구독 변경 - 다운그레이드이면 기존 구독을 기간 말 해지 처리하고 새 구독을 저장한다")
    void changeSubscription_downgrade_schedulesNewSubscription() throws IOException {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);
        BillingKey billingKey = mock(BillingKey.class);

        Subscription currentSubscription = mock(Subscription.class);
        Subscription newSubscription = mock(Subscription.class);

        Plan currentPlan = mostExpensivePlan();
        Plan targetPlan = cheapestPlan();

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(currentSubscription));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(userPlan.getPlan()).thenReturn(currentPlan);
        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.of(billingKey));

        try (MockedStatic<Subscription> mockedStatic = mockStatic(Subscription.class)) {
            mockedStatic.when(() ->
                    Subscription.downGradeSubscription(
                            user,
                            targetPlan,
                            billingKey,
                            currentSubscription
                    )
            ).thenReturn(newSubscription);

            // when
            subscriptionService.changeSubscription(userId, targetPlan);

            // then
            verify(currentSubscription).cancelAtPeriodEnd("Change the Plan");
            verify(subscriptionRepository).save(newSubscription);

            verify(billingKeyApprovalService, never()).chargeUpgradePlan(
                    any(),
                    any(),
                    any(),
                    anyLong()
            );
        }
    }

    @Test
    @DisplayName("구독 해지 - 활성 구독이면 기간 말 해지 처리한다")
    void cancelSubscription_activeSubscription_cancelAtPeriodEnd() {
        // given
        Long userId = 1L;
        String reason = "사용자 요청";

        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(subscription));
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);

        // when
        subscriptionService.cancelSubscription(userId, reason);

        // then
        verify(subscription).cancelAtPeriodEnd(reason);
    }

    @Test
    @DisplayName("구독 해지 - 이미 CANCELED 상태이면 아무 작업도 하지 않는다")
    void cancelSubscription_alreadyCanceled_doNothing() {
        // given
        Long userId = 1L;
        String reason = "사용자 요청";

        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(subscriptionRepository.findLatestByUser(eq(user), anyList()))
                .thenReturn(Optional.of(subscription));
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.CANCELED);

        // when
        subscriptionService.cancelSubscription(userId, reason);

        // then
        verify(subscription, never()).cancelAtPeriodEnd(anyString());
    }

    @Test
    @DisplayName("changeUserPlan - 해지 예약 구독의 기간이 끝나면 구독을 만료시키고 유저 플랜을 FREE로 변경한다")
    void changeUserPlan_expiredCancelScheduledSubscription_cancelToFree() {
        // given
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        UserPlan userPlan = mock(UserPlan.class);

        when(subscriptionRepository.findExpiredSubscriptions(
                eq(SubscriptionStatus.CANCEL_SCHEDULED),
                any(LocalDateTime.class)
        )).thenReturn(List.of(subscription));

        when(subscription.getUser()).thenReturn(user);
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));

        // when
        subscriptionService.cancelUserPlan();

        // then
        verify(subscription).expire();
        verify(userPlan).changePlan(Plan.FREE);
    }

    @Test
    @DisplayName("approveTodaySubscriptions - 오늘 결제 대상 구독을 조회해서 각각 결제를 시도한다")
    void approveTodaySubscriptions_payAllTargets() throws IOException {
        // given
        Subscription subscription1 = mock(Subscription.class);
        Subscription subscription2 = mock(Subscription.class);

        when(subscription2.getId()).thenReturn(2L);

        when(subscriptionRepository.findDailyBillingTargets(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0L),
                anyList(),
                any(PageRequest.class)
        )).thenReturn(List.of(subscription1, subscription2));

        when(subscriptionRepository.findDailyBillingTargets(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(2L),
                anyList(),
                any(PageRequest.class)
        )).thenReturn(List.of());

        // when
        subscriptionService.approveTodaySubscriptions();

        // then
        verify(billingKeyApprovalService).paySubscription(subscription1);
        verify(billingKeyApprovalService).paySubscription(subscription2);
    }

    @Test
    @DisplayName("approveTodaySubscriptions - 특정 구독 결제가 실패해도 다음 구독 결제를 계속 진행한다")
    void approveTodaySubscriptions_whenOnePaymentFails_continueNextPayment() throws IOException {
        // given
        Subscription subscription1 = mock(Subscription.class);
        Subscription subscription2 = mock(Subscription.class);

        when(subscription1.getId()).thenReturn(1L);
        when(subscription2.getId()).thenReturn(2L);

        when(subscriptionRepository.findDailyBillingTargets(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0L),
                anyList(),
                any(PageRequest.class)
        )).thenReturn(List.of(subscription1, subscription2));

        when(subscriptionRepository.findDailyBillingTargets(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(2L),
                anyList(),
                any(PageRequest.class)
        )).thenReturn(List.of());

        doThrow(new RuntimeException("결제 실패"))
                .when(billingKeyApprovalService)
                .paySubscription(subscription1);

        // when
        subscriptionService.approveTodaySubscriptions();

        // then
        verify(billingKeyApprovalService).paySubscription(subscription1);
        verify(billingKeyApprovalService).paySubscription(subscription2);
    }

    private Plan cheapestPlan() {
        return List.of(Plan.values()).stream()
                .min(Comparator.comparingLong(Plan::getPrice))
                .orElseThrow();
    }

    private Plan mostExpensivePlan() {
        return List.of(Plan.values()).stream()
                .max(Comparator.comparingLong(Plan::getPrice))
                .orElseThrow();
    }
}