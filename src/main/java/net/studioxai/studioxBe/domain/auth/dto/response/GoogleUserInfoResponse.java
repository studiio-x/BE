package net.studioxai.studioxBe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleUserInfoResponse {

    private String sub;
    private String email;
    private String name;

    @JsonProperty("picture")
    private String profileImage;

    @JsonProperty("email_verified")
    private boolean emailVerified;
}
