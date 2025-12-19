package net.studioxai.studioxBe.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTokenResult {
    private String accessToken;
    private String refreshToken;
}

