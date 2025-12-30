package net.studioxai.studioxBe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(

        String sub,
        String email,
        String name,

        @JsonProperty("picture")
        String profileImage,

        @JsonProperty("email_verified")
        boolean emailVerified
) {
}
