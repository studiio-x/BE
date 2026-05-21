package net.studioxai.studioxBe.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.service.SubscriptionService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    @DeleteMapping("/v1/Subscription")
    public void subscriptionCancel(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody String reason
    ) {
        subscriptionService.cancelSubscription(principal.userId(), reason);
    }
}
