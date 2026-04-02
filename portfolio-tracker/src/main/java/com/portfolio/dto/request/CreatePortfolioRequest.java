package com.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePortfolioRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    private String name;

    private String description;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    private String currency = "USD";
}

