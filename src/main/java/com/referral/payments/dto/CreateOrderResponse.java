package com.referral.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {

    private String orderId;
    private String currency;
    private Long amountInPaise;

    /**
     * Public Razorpay key id, used by frontend to initialize Razorpay checkout.
     */
    private String razorpayKeyId;
}


