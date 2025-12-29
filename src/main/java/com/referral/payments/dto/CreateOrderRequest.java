package com.referral.payments.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull
    private Long userId;

    /**
     * How many referral quota units this purchase should grant.
     */
    @Min(1)
    private int quotaUnits;

    /**
     * Amount in smallest currency unit (e.g., paise for INR).
     */
    @Min(1)
    private Long amountInPaise;

    private String currency = "INR";
}


