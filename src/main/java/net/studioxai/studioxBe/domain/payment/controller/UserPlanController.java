package net.studioxai.studioxBe.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.dto.response.MyPlanResponse;
import net.studioxai.studioxBe.domain.payment.service.UserPlanService;
import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserPlanController {
    private final UserPlanService userPlanService;

    public MyPlanResponse getMyPlan(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ){
        return userPlanService.getUserPlan(principal.userId());
    }
}
