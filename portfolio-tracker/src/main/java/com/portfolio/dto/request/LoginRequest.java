package com.portfolio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "mypassword123")
    private String password;
}
