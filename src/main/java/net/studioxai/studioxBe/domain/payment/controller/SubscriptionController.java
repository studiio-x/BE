package net.studioxai.studioxBe.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.repository.SubscriptionRepository;
import net.studioxai.studioxBe.domain.payment.service.SubscriptionService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @DeleteMapping("/v1/subscription")
    public void subscriptionCancel(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestBody String reason
    ) {
        subscriptionService.cancelSubscription(principal.userId(), reason);
    }

    @PatchMapping("/v1/subscription")
    public void subscriptionChange(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Plan plan
    ) throws IOException {
        subscriptionService.changeSubscription(principal.userId(), plan);
    }

    @GetMapping("/v1/subscription/price")
    public long getSubscriptionPrice(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Plan plan
    ) {
        return subscriptionService.getSubscriptionPrice(principal.userId(), plan);
    }
}
