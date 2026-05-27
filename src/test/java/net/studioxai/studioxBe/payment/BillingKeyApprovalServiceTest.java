package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.dto.FailureDto;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingApprovalRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.PaymentApprovalResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.entity.enums.SubscriptionStatus;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyExceptionHandler;
import net.studioxai.studioxBe.domain.payment.exception.UserPlanExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.PaymentHistoryRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.repository.UserPlanRepository;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyApprovalService;
import net.studioxai.studioxBe.domain.payment.service.ExchangeRateService;
import net.studioxai.studioxBe.domain.payment.service.TossService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.util.IpUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingKeyApprovalServiceTest {

    @InjectMocks
    private BillingKeyApprovalService billingKeyApprovalService;

    @Mock
    private IpUtil ipUtil;

    @Mock
    private TossService tossService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private BillingKeyRepository billingKeyRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private User user;

    @Mock
    private BillingKey billingKey;

    @Mock
    private UserPlan userPlan;

    @Test
    @DisplayName("approveBilling: 결제 성공 시 구독을 저장하고 유저 플랜을 월 초기화한다")
    void approveBilling_success() throws IOException {
        Plan plan = paidPlan();
        String clientIp = "127.0.0.1";
        PaymentApprovalResponse response = successResponse();

        when(billingKey.getBillingKey()).thenReturn("billing-key-123");
        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.of(billingKey));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1400));

        when(tossService.getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        )).thenReturn(response);

        billingKeyApprovalService.approveBilling(user, plan, clientIp);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(userPlan).montlyInitialize();

        verify(tossService).getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        );
    }

    @Test
    @DisplayName("approveBilling: BillingKey가 없으면 예외가 발생한다")
    void approveBilling_withoutBillingKey_throwsException() throws IOException {
        Plan plan = paidPlan();

        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingKeyApprovalService.approveBilling(user, plan, "127.0.0.1"))
                .isInstanceOf(BillingKeyExceptionHandler.class);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(userPlanRepository, never()).findByUser(any());
        verify(tossService, never()).getResponse(any(), any(), anyString());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveBilling: UserPlan이 없으면 예외가 발생한다")
    void approveBilling_withoutUserPlan_throwsException() throws IOException {
        Plan plan = paidPlan();

        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.of(billingKey));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingKeyApprovalService.approveBilling(user, plan, "127.0.0.1"))
                .isInstanceOf(UserPlanExceptionHandler.class);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(tossService, never()).getResponse(any(), any(), anyString());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("paySubscription: 결제 성공 + CHANGE_SCHEDULED 상태면 플랜 변경 후 구독을 갱신한다")
    void paySubscription_success_changeScheduled() throws IOException {
        Plan plan = paidPlan();
        Subscription subscription = mock(Subscription.class);
        PaymentApprovalResponse response = successResponse();

        when(subscription.getUser()).thenReturn(user);
        when(subscription.getPlan()).thenReturn(plan);
        when(subscription.getBillingKey()).thenReturn(billingKey);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.CHANGE_SCHEDULED);

        when(billingKey.getBillingKey()).thenReturn("billing-key-123");
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1400));

        when(tossService.getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        )).thenReturn(response);

        billingKeyApprovalService.paySubscription(subscription);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(userPlan).changePlan(plan);
        verify(subscription).renew();
        verify(userPlan).montlyInitialize();
        verify(subscription, never()).markBillingFailed(any(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("paySubscription: 결제 실패 시 구독을 결제 실패 상태로 표시한다")
    void paySubscription_failed_markBillingFailed() throws IOException {
        Plan plan = paidPlan();
        Subscription subscription = mock(Subscription.class);
        PaymentApprovalResponse response = failedResponse();

        when(subscription.getUser()).thenReturn(user);
        when(subscription.getPlan()).thenReturn(plan);
        when(subscription.getBillingKey()).thenReturn(billingKey);

        when(billingKey.getBillingKey()).thenReturn("billing-key-123");
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1400));

        when(tossService.getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        )).thenReturn(response);

        billingKeyApprovalService.paySubscription(subscription);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(subscription).markBillingFailed(eq("카드 승인 실패"), any(LocalDateTime.class));

        verify(subscription, never()).renew();
        verify(userPlan, never()).montlyInitialize();
        verify(userPlan, never()).changePlan(any());
    }

    @Test
    @DisplayName("chargeUpgradePlan: 업그레이드 결제 실패 시 기존 구독을 결제 실패 상태로 표시한다")
    void chargeUpgradePlan_failed_markBillingFailed() throws IOException {
        Plan newPlan = paidPlan();
        Subscription subscription = mock(Subscription.class);
        PaymentApprovalResponse response = failedResponse();

        when(billingKey.getBillingKey()).thenReturn("billing-key-123");
        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.of(billingKey));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));

        when(tossService.getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        )).thenReturn(response);

        billingKeyApprovalService.chargeUpgradePlan(user, subscription, newPlan, 10_000L);

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(subscription).markBillingFailed(eq("카드 승인 실패"), any(LocalDateTime.class));

        verify(subscription, never()).cancelAtPeriodEnd(anyString());
        verify(subscriptionRepository, never()).save(any());
        verify(userPlan, never()).changePlan(any());
    }

    @Test
    @DisplayName("updatePaymentHistory: Toss status가 DONE이면 PaymentStatus.SUCCESS로 저장한다")
    void updatePaymentHistory_success() {
        PaymentHistory paymentHistory = mock(PaymentHistory.class);
        PaymentApprovalResponse response = successResponse();

        billingKeyApprovalService.updatePaymentHistory(paymentHistory, response);

        verify(paymentHistory).updatePaymentResult(
                eq("payment-key-1"),
                eq(14000L),
                eq("CARD"),
                eq(PaymentStatus.SUCCESS),
                eq("2026-05-26T10:00:00+09:00"),
                eq("2026-05-26T10:00:03+09:00"),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("updatePaymentHistory: Toss status가 DONE이 아니면 PaymentStatus.FAILED와 실패 정보를 저장한다")
    void updatePaymentHistory_failed() {
        PaymentHistory paymentHistory = mock(PaymentHistory.class);
        PaymentApprovalResponse response = failedResponse();

        billingKeyApprovalService.updatePaymentHistory(paymentHistory, response);

        verify(paymentHistory).updatePaymentResult(
                eq("payment-key-2"),
                eq(14000L),
                eq("CARD"),
                eq(PaymentStatus.FAILED),
                eq("2026-05-26T10:00:00+09:00"),
                isNull(),
                eq("PAY_PROCESS_CANCELED"),
                eq("카드 승인 실패")
        );
    }

    @Test
    @DisplayName("approveBilling: 결제 성공 시 PaymentHistory 상태가 SUCCESS로 변경된다")
    void approveBilling_success_updatePaymentHistory() throws IOException {
        Plan plan = paidPlan();
        PaymentApprovalResponse response = successResponse();

        when(billingKey.getBillingKey()).thenReturn("billing-key-123");
        when(billingKeyRepository.findByUser(user)).thenReturn(Optional.of(billingKey));
        when(userPlanRepository.findByUser(user)).thenReturn(Optional.of(userPlan));
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1400));

        when(tossService.getResponse(
                any(BillingApprovalRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/billing/billing-key-123")
        )).thenReturn(response);

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);

        billingKeyApprovalService.approveBilling(user, plan, "127.0.0.1");

        verify(paymentHistoryRepository).save(captor.capture());

        PaymentHistory savedPaymentHistory = captor.getValue();

        assertThat(savedPaymentHistory.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(savedPaymentHistory.getPaymentKey()).isEqualTo("payment-key-1");
        assertThat(savedPaymentHistory.getMethod()).isEqualTo("CARD");
    }

    private Plan paidPlan() {
        return Arrays.stream(Plan.values())
                .filter(plan -> plan.getPrice() > 0)
                .findFirst()
                .orElse(Plan.values()[0]);
    }

    private PaymentApprovalResponse successResponse() {
        PaymentApprovalResponse response = mock(PaymentApprovalResponse.class);

        when(response.status()).thenReturn("DONE");
        when(response.paymentKey()).thenReturn("payment-key-1");
        when(response.totalAmount()).thenReturn(BigDecimal.valueOf(14000));
        when(response.method()).thenReturn("CARD");
        when(response.requestedAt()).thenReturn("2026-05-26T10:00:00+09:00");
        when(response.approvedAt()).thenReturn("2026-05-26T10:00:03+09:00");
        when(response.failure()).thenReturn(null);

        return response;
    }

    private PaymentApprovalResponse failedResponse() {
        PaymentApprovalResponse response = mock(PaymentApprovalResponse.class);
        FailureDto failure = mock(FailureDto.class);

        when(failure.code()).thenReturn("PAY_PROCESS_CANCELED");
        when(failure.message()).thenReturn("카드 승인 실패");

        when(response.status()).thenReturn("FAILED");
        when(response.paymentKey()).thenReturn("payment-key-2");
        when(response.totalAmount()).thenReturn(BigDecimal.valueOf(14000));
        when(response.method()).thenReturn("CARD");
        when(response.requestedAt()).thenReturn("2026-05-26T10:00:00+09:00");
        when(response.approvedAt()).thenReturn(null);
        when(response.failure()).thenReturn(failure);

        return response;
    }
}