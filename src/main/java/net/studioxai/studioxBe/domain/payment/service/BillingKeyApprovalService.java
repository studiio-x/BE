package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingApprovalRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.BillingApprovalResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.exception.*;
import net.studioxai.studioxBe.domain.payment.repository.*;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.util.IpUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    private final UserPlanRepository userPlanRepository;

    @Transactional
    public void approveBilling(User user, Plan plan, String clientIp) throws IOException {
        PaymentHistory paymentHistory = savePaymentHistory(user, plan);

        BillingKey billingKey = billingKeyRepository.findByUser(user).orElseThrow(
                () -> new BillingKeyExceptionHandler(BillingKeyErrorCode.NOT_FOUND_BILLING_KEY)
        );

        UserPlan userPlan = userPlanRepository.findByUser(user).orElseThrow(
                () -> new UserPlanExceptionHandler(UserPlanErrorCode.USER_PLAN_NOT_FOUNT)
        );

        long amount = plan.getPrice() * exchangeRateService.getKrwRate()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        BillingApprovalRequest request = BillingApprovalRequest.of(user, plan, amount, billingKey, paymentHistory, clientIp, 0, 0);

        BillingApprovalResponse response = tossService.getResponse(request, BillingApprovalResponse.class, "/v1/billing/"+billingKey.getBillingKey());

        updatePaymentHistory(paymentHistory, response);
        if (paymentHistory.getStatus() == PaymentStatus.SUCCESS) {
            Subscription subscription = Subscription.createSubscription(user, plan, billingKey);
            subscriptionRepository.save(subscription);

            userPlan.montlyInitialize();
        }
        // TODO: 첫 결제 실패 정책 수립 후 작성 예정

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
                () -> new SubscriptionExceptionHandler(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND)
        );

        User user = subscription.getUser();
        Plan plan = subscription.getPlan();
        BillingKey billingKey = subscription.getBillingKey();

        UserPlan userPlan = userPlanRepository.findByUser(user).orElseThrow(
                () -> new UserPlanExceptionHandler(UserPlanErrorCode.USER_PLAN_NOT_FOUNT)
        );

        PaymentHistory paymentHistory = savePaymentHistory(user, plan);

        long amount = plan.getPrice() * exchangeRateService.getKrwRate()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        BillingApprovalRequest request = BillingApprovalRequest.of(user, plan, amount, billingKey, paymentHistory, null, 0, 0);

        BillingApprovalResponse response = tossService.getResponse(request, BillingApprovalResponse.class, "/v1/billing/"+billingKey.getBillingKey());

        updatePaymentHistory(paymentHistory, response);

        if (paymentHistory.getStatus() == PaymentStatus.SUCCESS) {
            subscription.renew();
            userPlan.montlyInitialize();
        }
        else {
            LocalDateTime now = LocalDateTime.now();
            subscription.markBillingFailed(paymentHistory.getFailureMessage(), now);
        }


    }




}
