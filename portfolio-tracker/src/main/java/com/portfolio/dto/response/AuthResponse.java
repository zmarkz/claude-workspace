package com.portfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT authentication response")
public class AuthResponse {

    @Schema(description = "JWT Bearer token (valid 24 hours)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;
}
