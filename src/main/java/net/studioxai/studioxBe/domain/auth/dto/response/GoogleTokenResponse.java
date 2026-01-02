package net.studioxai.studioxBe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("token_type")
        String tokenType,

        String scope
) {
}

