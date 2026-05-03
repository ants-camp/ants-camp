package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("expires_in")
        String tokenExpire,
        @JsonProperty("access_token_token_expired")
        String tokenExpireTime

) {
        public static AccessTokenResponse create(String accessToken, String tokenType, String tokenExpire, String tokenExpireTime){
                return new AccessTokenResponse(
                        accessToken,
                        tokenType,
                        tokenExpire,
                        tokenExpireTime
                );
        }
}
