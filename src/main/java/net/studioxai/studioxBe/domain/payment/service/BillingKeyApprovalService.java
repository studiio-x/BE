package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingApprovalRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.BillingApprovalResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyExceptionHandler;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.SubscriptionExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.ExchangeRateRepository;
import net.studioxai.studioxBe.domain.payment.repository.PaymentHistoryRepository;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.util.IpUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BillingKeyApprovalService {
    private final IpUtil ipUtil;

    private final TossService tossService;
    private final ExchangeRateService exchangeRateService;

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void approveBilling(User user, Plan plan, String clientIp) throws IOException {
        PaymentHistory paymentHistory = savePaymentHistory(user, plan);

        BillingKey billingKey = billingKeyRepository.findByUser(user).orElseThrow(
                () -> new BillingKeyExceptionHandler(BillingKeyErrorCode.NOT_FOUND_BILLING_KEY)
        );

        long amount = exchangeRateService.getKrwRate()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        BillingApprovalRequest request = BillingApprovalRequest.of(user, plan, amount, billingKey, paymentHistory, clientIp, 0, 0);

        BillingApprovalResponse response = tossService.getResponse(request, BillingApprovalResponse.class, "/v1/billing/"+billingKey.getBillingKey());

        updatePaymentHistory(paymentHistory, response);

        Subscription subscription = Subscription.createSubscription(user, plan, billingKey);
        subscriptionRepository.save(subscription);
    }

    private PaymentHistory savePaymentHistory(User user, Plan plan) {
        String orderId = UUID.randomUUID().toString();
        PaymentHistory paymentHistory = PaymentHistory.createPaymentHistory(user, orderId, plan.getPrice());
        paymentHistoryRepository.save(paymentHistory);
        return paymentHistory;
    }

    private void updatePaymentHistory(PaymentHistory paymentHistory, BillingApprovalResponse response) {
        PaymentStatus paymentStatus =
                "DONE".equals(response.status())
                        ? PaymentStatus.SUCCESS
                        : PaymentStatus.FAILED;

        String failureCode = response.failure() != null
                ? response.failure().code()
                : null;

        String failureMessage = response.failure() != null
                ? response.failure().message()
                : null;

        paymentHistory.updatePaymentResult(
                response.paymentKey(),
                response.totalAmount().setScale(0, RoundingMode.HALF_UP).longValue(),
                response.method(),
                paymentStatus,
                response.requestedAt(),
                response.approvedAt(),
                failureCode,
                failureMessage
        );
    }

    public void paySubscription(Long subscriptionId) throws IOException {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new SubscriptionExceptionHandler(SubscriptionErrorCode.NOT_FOUND_SUBSCRIPTION)
        );

        User user = subscription.getUser();
        Plan plan = subscription.getPlan();
        BillingKey billingKey = subscription.getBillingKey();

        PaymentHistory paymentHistory = savePaymentHistory(user, plan);

        long amount = exchangeRateService.getKrwRate()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        BillingApprovalRequest request = BillingApprovalRequest.of(user, plan, amount, billingKey, paymentHistory, null, 0, 0);

        BillingApprovalResponse response = tossService.getResponse(request, BillingApprovalResponse.class, "/v1/billing/"+billingKey.getBillingKey());

        updatePaymentHistory(paymentHistory, response);

        if (paymentHistory.getStatus() == PaymentStatus.SUCCESS) {
            subscription.renew();
        }
    }




}
