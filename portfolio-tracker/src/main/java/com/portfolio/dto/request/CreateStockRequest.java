package com.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStockRequest {

    @NotBlank
    private String symbol;

    @NotBlank
    private String companyName;

    @NotBlank
    private String exchange;

    private String sector;
    private String industry;
}
