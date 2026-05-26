package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.dto.request.CreditPaymentRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.PaymentApprovalResponse;
import net.studioxai.studioxBe.domain.payment.entity.ExtraCredit;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.enums.CreditOption;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.ExtraCreditRepository;
import net.studioxai.studioxBe.domain.payment.repository.PaymentHistoryRepository;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyApprovalService;
import net.studioxai.studioxBe.domain.payment.service.CreditPaymentService;
import net.studioxai.studioxBe.domain.payment.service.ExchangeRateService;
import net.studioxai.studioxBe.domain.payment.service.TossService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditPaymentServiceTest {

    @InjectMocks
    private CreditPaymentService creditPaymentService;

    @Mock
    private UserService userService;

    @Mock
    private TossService tossService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private BillingKeyApprovalService billingKeyApprovalService;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private BillingKeyRepository billingKeyRepository;

    @Mock
    private ExtraCreditRepository extraCreditRepository;

    @Test
    @DisplayName("크레딧 결제 성공 시 결제 내역을 저장하고 ExtraCredit을 생성한다")
    void buyCredit_success_saveExtraCredit() throws IOException {
        // given
        Long userId = 1L;
        String paymentKey = "payment-key-test";

        // TODO: 실제 CreditOption enum 값으로 변경
        CreditOption option = CreditOption.CREDIT_100;

        User user = mock(User.class);
        PaymentApprovalResponse response = mock(PaymentApprovalResponse.class);

        BigDecimal krwRate = BigDecimal.valueOf(1300.5);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(krwRate);

        when(tossService.getResponse(
                any(CreditPaymentRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        )).thenReturn(response);

        doAnswer(invocation -> {
            PaymentHistory paymentHistory = invocation.getArgument(0);
            ReflectionTestUtils.setField(paymentHistory, "status", PaymentStatus.SUCCESS);
            return null;
        }).when(billingKeyApprovalService)
                .updatePaymentHistory(any(PaymentHistory.class), eq(response));

        // when
        creditPaymentService.buyCredit(userId, option, paymentKey);

        // then
        verify(paymentHistoryRepository).save(any(PaymentHistory.class));

        verify(tossService).getResponse(
                any(CreditPaymentRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        );

        verify(billingKeyApprovalService)
                .updatePaymentHistory(any(PaymentHistory.class), eq(response));

        ArgumentCaptor<ExtraCredit> extraCreditCaptor = ArgumentCaptor.forClass(ExtraCredit.class);
        verify(extraCreditRepository).save(extraCreditCaptor.capture());

        ExtraCredit savedExtraCredit = extraCreditCaptor.getValue();

        assertThat(ReflectionTestUtils.getField(savedExtraCredit, "user"))
                .isEqualTo(user);

        assertThat(ReflectionTestUtils.getField(savedExtraCredit, "credit"))
                .isEqualTo(option);
    }

    @Test
    @DisplayName("크레딧 결제 실패 시 ExtraCredit을 생성하지 않는다")
    void buyCredit_fail_doNotSaveExtraCredit() throws IOException {
        // given
        Long userId = 1L;
        String paymentKey = "payment-key-test";

        // TODO: 실제 CreditOption enum 값으로 변경
        CreditOption option = CreditOption.CREDIT_100;

        User user = mock(User.class);
        PaymentApprovalResponse response = mock(PaymentApprovalResponse.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));

        when(tossService.getResponse(
                any(CreditPaymentRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        )).thenReturn(response);

        doAnswer(invocation -> {
            PaymentHistory paymentHistory = invocation.getArgument(0);
            ReflectionTestUtils.setField(paymentHistory, "status", PaymentStatus.FAILED);
            return null;
        }).when(billingKeyApprovalService)
                .updatePaymentHistory(any(PaymentHistory.class), eq(response));

        // when
        creditPaymentService.buyCredit(userId, option, paymentKey);

        // then
        verify(paymentHistoryRepository).save(any(PaymentHistory.class));

        verify(billingKeyApprovalService)
                .updatePaymentHistory(any(PaymentHistory.class), eq(response));

        verify(extraCreditRepository, never()).save(any(ExtraCredit.class));
    }

    @Test
    @DisplayName("Toss 결제 승인 요청 금액은 옵션 가격과 환율을 곱한 뒤 반올림한 값이다")
    void buyCredit_requestAmountIsRoundedKrwAmount() throws IOException {
        // given
        Long userId = 1L;
        String paymentKey = "payment-key-test";

        // TODO: 실제 CreditOption enum 값으로 변경
        CreditOption option = CreditOption.CREDIT_100;

        User user = mock(User.class);
        PaymentApprovalResponse response = mock(PaymentApprovalResponse.class);

        BigDecimal krwRate = BigDecimal.valueOf(1300.6);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(krwRate);

        when(tossService.getResponse(
                any(CreditPaymentRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        )).thenReturn(response);

        doAnswer(invocation -> {
            PaymentHistory paymentHistory = invocation.getArgument(0);
            ReflectionTestUtils.setField(paymentHistory, "status", PaymentStatus.SUCCESS);
            return null;
        }).when(billingKeyApprovalService)
                .updatePaymentHistory(any(PaymentHistory.class), eq(response));

        long expectedAmount = option.getPrice() * krwRate
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        ArgumentCaptor<CreditPaymentRequest> requestCaptor =
                ArgumentCaptor.forClass(CreditPaymentRequest.class);

        // when
        creditPaymentService.buyCredit(userId, option, paymentKey);

        // then
        verify(tossService).getResponse(
                requestCaptor.capture(),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        );

        CreditPaymentRequest request = requestCaptor.getValue();

        assertThat(ReflectionTestUtils.getField(request, "paymentKey"))
                .isEqualTo(paymentKey);

        assertThat(ReflectionTestUtils.getField(request, "amount"))
                .isEqualTo(expectedAmount);

        assertThat(ReflectionTestUtils.getField(request, "orderId"))
                .isNotNull();
    }

    @Test
    @DisplayName("Toss 결제 승인 중 IOException이 발생하면 예외가 그대로 전파되고 ExtraCredit은 생성되지 않는다")
    void buyCredit_tossIOException_throwException() throws IOException {
        // given
        Long userId = 1L;
        String paymentKey = "payment-key-test";

        // TODO: 실제 CreditOption enum 값으로 변경
        CreditOption option = CreditOption.CREDIT_100;

        User user = mock(User.class);

        when(userService.getUserByIdOrThrow(userId)).thenReturn(user);
        when(exchangeRateService.getKrwRate()).thenReturn(BigDecimal.valueOf(1300));

        when(tossService.getResponse(
                any(CreditPaymentRequest.class),
                eq(PaymentApprovalResponse.class),
                eq("/v1/payments/confirm")
        )).thenThrow(new IOException("Toss API error"));

        // when & then
        assertThatThrownBy(() ->
                creditPaymentService.buyCredit(userId, option, paymentKey)
        ).isInstanceOf(IOException.class)
                .hasMessageContaining("Toss API error");

        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
        verify(billingKeyApprovalService, never())
                .updatePaymentHistory(any(PaymentHistory.class), any(PaymentApprovalResponse.class));
        verify(extraCreditRepository, never()).save(any(ExtraCredit.class));
    }
}