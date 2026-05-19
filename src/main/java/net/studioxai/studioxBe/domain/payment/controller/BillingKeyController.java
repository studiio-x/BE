package net.studioxai.studioxBe.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyAuthKeyCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyCardCreateRequest;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillingKeyController {
    private final BillingKeyService billingKeyService;

    @PostMapping("/v1/payment/billingKey/authKey")
    public void createBillingKeyAuthKey(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody BillingKeyAuthKeyCreateRequest billingKeyCreateRequest
    ) throws IOException {
        billingKeyService.createBillingKeyWithAuthKey(principal.userId(), billingKeyCreateRequest);
    }

    @PostMapping("/v1/payment/billingKey/card")
    public void createBillingKeyCard(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody BillingKeyCardCreateRequest billingKeyCreateRequest
    ) throws IOException {
        billingKeyService.createBillingKeyWithCard(principal.userId(), billingKeyCreateRequest);
    }

}
