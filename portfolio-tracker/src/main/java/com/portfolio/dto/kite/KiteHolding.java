package com.portfolio.dto.kite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KiteHolding {

    @JsonProperty("tradingsymbol")
    private String tradingsymbol;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("average_price")
    private double averagePrice;

    @JsonProperty("last_price")
    private double lastPrice;

    @JsonProperty("pnl")
    private double pnl;

    @JsonProperty("isin")
    private String isin;

    @JsonProperty("product")
    private String product;
}
