package net.studioxai.studioxBe.domain.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyAuthKeyCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyCardCreateRequest;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import net.studioxai.studioxBe.global.util.IpUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillingKeyController {
    private final IpUtil ipUtil;
    private final BillingKeyService billingKeyService;

    @PostMapping("/v1/payment/billingKey/authKey")
    public void createBillingKeyAuthKey(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Plan plan,
            @RequestBody BillingKeyAuthKeyCreateRequest billingKeyCreateRequest,
            HttpServletRequest request
    ) throws IOException {
        String clientIp = ipUtil.getClientIp(request);
        billingKeyService.createBillingKeyWithAuthKey(principal.userId(), billingKeyCreateRequest, plan, clientIp);
    }

    @PostMapping("/v1/payment/billingKey/card")
    public void createBillingKeyCard(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Plan plan,
            @RequestBody BillingKeyCardCreateRequest billingKeyCreateRequest,
            HttpServletRequest request
    ) throws IOException {
        String clientIp = ipUtil.getClientIp(request);
        billingKeyService.createBillingKeyWithCard(principal.userId(), billingKeyCreateRequest, plan, clientIp);
    }

}
