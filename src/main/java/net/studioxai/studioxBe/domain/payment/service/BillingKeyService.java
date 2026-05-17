package net.studioxai.studioxBe.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.payment.dto.CardDto;
import net.studioxai.studioxBe.domain.payment.dto.TransferDto;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyAuthKeyCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyCardCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.BillingKeyResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyErrorCode;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.util.JsonUtil;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BillingKeyService {
    private final UserService userService;
    private final TossService tossService;

    private final JsonUtil jsonUtil;

    private final BillingKeyRepository billingKeyRepository;

    @Transactional
    public void createBillingKeyWithAuthKey(
            Long userId,
            BillingKeyAuthKeyCreateRequest billingKeyAuthKeyCreateRequest
    ) throws IOException {
        User user = userService.getUserByIdOrThrow(userId);

        if(!user.equalsCustomerKey(billingKeyAuthKeyCreateRequest.customerKey())) {
            throw new BillingKeyExceptionHandler(BillingKeyErrorCode.INVALID_CUSTOM_KEY);
        };

        BillingKeyResponse response = tossService.getResponse(billingKeyAuthKeyCreateRequest, BillingKeyResponse.class, "/v1/billing/authorizations/issue");

        BillingKey billingKey = toEntity(user, response);
        saveBillingKey(billingKey);
    }

    @Transactional
    public void createBillingKeyWithCard(
            Long userId,
            BillingKeyCardCreateRequest billingKeyCardCreateRequest
    ) throws IOException {
        User user = userService.getUserByIdOrThrow(userId);

        if(!user.equalsCustomerKey(billingKeyCardCreateRequest.customerKey())) {
            throw new BillingKeyExceptionHandler(BillingKeyErrorCode.INVALID_CUSTOM_KEY);
        };

        BillingKeyResponse response = tossService.getResponse(billingKeyCardCreateRequest, BillingKeyResponse.class, "/v1/billing/authorizations/card");

        BillingKey billingKey = toEntity(user, response);
        saveBillingKey(billingKey);

    }

    private void saveBillingKey(BillingKey billingKey) {
        billingKeyRepository.save(billingKey);
    }


    private BillingKey toEntity(User user, BillingKeyResponse response) {
        CardDto card = response.card();

        TransferDto transfer =
                response.transfers() != null && !response.transfers().isEmpty()
                        ? response.transfers().get(0)
                        : null;

        return BillingKey.create(
                user,
                response.billingKey(),
                response.method(),
                transfer != null ? transfer.bankName() : null,
                transfer != null ? transfer.bankAccountNumber() : null,
                card != null ? card.issuerCode() : null,
                card != null ? card.acquirerCode() : null,
                card != null ? card.number() : null
        );
    }

}
