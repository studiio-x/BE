package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.request.CreditPaymentRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.PaymentApprovalResponse;
import net.studioxai.studioxBe.domain.payment.entity.ExtraCredit;
import net.studioxai.studioxBe.domain.payment.entity.PaymentHistory;
import net.studioxai.studioxBe.domain.payment.entity.Subscription;
import net.studioxai.studioxBe.domain.payment.entity.enums.CreditOption;
import net.studioxai.studioxBe.domain.payment.entity.enums.PaymentStatus;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.repository.ExtraCreditRepository;
import net.studioxai.studioxBe.domain.payment.repository.PaymentHistoryRepository;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CreditPaymentService {
    private final UserService userService;
    private final TossService tossService;
    private final ExchangeRateService exchangeRateService;
    private final BillingKeyApprovalService billingKeyApprovalService;

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final ExtraCreditRepository extraCreditRepository;

    @Transactional
    public void buyCredit(Long userId, CreditOption option, String paymentKey) throws IOException {
        User user = userService.getUserByIdOrThrow(userId);

        String orderId = UUID.randomUUID().toString();
        PaymentHistory paymentHistory = PaymentHistory.createPaymentHistory(user, orderId, option.getPrice());
        paymentHistoryRepository.save(paymentHistory);

        long amount = option.getPrice() * exchangeRateService.getKrwRate()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        CreditPaymentRequest creditPaymentRequest = new CreditPaymentRequest(paymentKey, orderId, amount);

        PaymentApprovalResponse response = tossService.getResponse(creditPaymentRequest, PaymentApprovalResponse.class, "/v1/payments/confirm");

        billingKeyApprovalService.updatePaymentHistory(paymentHistory, response);

        if (paymentHistory.getStatus() == PaymentStatus.SUCCESS) {
            ExtraCredit extraCredit = ExtraCredit.create(user, option);
            extraCreditRepository.save(extraCredit);
        }


    }

}
