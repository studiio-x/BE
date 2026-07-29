package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.dto.ExtraCreditSummaryDto;
import net.studioxai.studioxBe.domain.payment.dto.response.MyPlanResponse;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionExceptionHandler;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.ExtraCreditRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.payment.service.UserPlanService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPlanServiceTest {

    @InjectMocks
    private UserPlanService userPlanService;

    @Mock
    private UserService userService;

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ExtraCreditRepository extraCreditRepository;

    @Test
    @DisplayName("내 플랜 조회 성공")
    void getUserPlan_success() {
        // given
        Long userId = 1L;

        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);
        Subscription subscription = mock(Subscription.class);

        Plan plan = Plan.values()[0];

        int usedCredit = 10;
        int extraCreditAmount = 50;

        LocalDateTime periodStart = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime nearestExpiredAt = LocalDateTime.of(2026, 5, 30, 23, 59);

        ExtraCreditSummaryDto extraCreditSummary =
                new ExtraCreditSummaryDto(extraCreditAmount, nearestExpiredAt);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));

        when(userPlan.getPlan()).thenReturn(plan);
        when(userPlan.getCredit()).thenReturn(usedCredit);

        when(subscriptionRepository.findLatestByUser(
                eq(user),
                eq(List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.CHANGE_SCHEDULED
                ))
        )).thenReturn(Optional.of(subscription));

        when(subscription.getPlan()).thenReturn(plan);
        when(subscription.getCurrentPeriodStart()).thenReturn(periodStart);
        when(subscription.getCurrentPeriodEnd()).thenReturn(periodEnd);

        when(extraCreditRepository.findAvailableCreditSummary(eq(userId), any(LocalDateTime.class)))
                .thenReturn(extraCreditSummary);

        // when
        MyPlanResponse response = userPlanService.getUserPlan(userId);

        // then
        assertThat(response).isNotNull();

        assertThat(response.totalCredit())
                .isEqualTo(plan.getCredit() + extraCreditAmount - usedCredit);

        assertThat(response.subscriptionCredit())
                .isEqualTo(plan.getCredit() - usedCredit);

        assertThat(response.extraCredit())
                .isEqualTo(extraCreditAmount);

        assertThat(response.creditExpirationDate())
                .isEqualTo(nearestExpiredAt);

        assertThat(response.subscriptionPrice())
                .isEqualTo(plan.getPrice());

        assertThat(response.subscriptionPaymentDate())
                .isEqualTo(periodStart);

        assertThat(response.subscriptionExpirationDate())
                .isEqualTo(periodEnd);

        assertThat(response.userPlan())
                .isEqualTo(plan);

        verify(userService).getUserByIdOrThrow(userId);
        verify(userPlanRepository).findByUser(user);
        verify(subscriptionRepository).findLatestByUser(
                eq(user),
                eq(List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.CHANGE_SCHEDULED
                ))
        );
        verify(extraCreditRepository).findAvailableCreditSummary(eq(userId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("유저 플랜이 없으면 예외가 발생한다")
    void getUserPlan_userPlanNotFound() {
        // given
        Long userId = 1L;
        User user = mock(User.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userPlanService.getUserPlan(userId))
                .isInstanceOf(UserPlanExceptionHandler.class);

        verify(userService).getUserByIdOrThrow(userId);
        verify(userPlanRepository).findByUser(user);

        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(extraCreditRepository);
    }

    @Test
    @DisplayName("활성 구독 정보가 없으면 예외가 발생한다")
    void getUserPlan_subscriptionNotFound() {
        // given
        Long userId = 1L;

        User user = mock(User.class);
        UserPlan userPlan = mock(UserPlan.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));

        when(subscriptionRepository.findLatestByUser(
                eq(user),
                eq(List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.CHANGE_SCHEDULED
                ))
        )).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userPlanService.getUserPlan(userId))
                .isInstanceOf(SubscriptionExceptionHandler.class);

        verify(userService).getUserByIdOrThrow(userId);
        verify(userPlanRepository).findByUser(user);
        verify(subscriptionRepository).findLatestByUser(
                eq(user),
                eq(List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.CHANGE_SCHEDULED
                ))
        );

        verifyNoInteractions(extraCreditRepository);
    }
}