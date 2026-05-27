package net.studioxai.studioxBe.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.CreditOption;
import net.studioxai.studioxBe.domain.payment.service.CreditPaymentService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CreditController {
    private final CreditPaymentService creditPaymentService;

    @PostMapping("/v1/payment/credit")
    public void buyCredit(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam CreditOption option,
            @RequestParam String paymentKey
    ) throws IOException {
        creditPaymentService.buyCredit(principal.userId(), option, paymentKey);
    }


}
