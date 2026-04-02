package com.portfolio.dto.kite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KiteSession {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("email")
    private String email;
}
