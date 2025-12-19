package net.studioxai.studioxBe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleUserInfoResponse {

    private String sub;
    private String email;
    private String name;

    @JsonProperty("email_verified")
    private boolean emailVerified;
}
