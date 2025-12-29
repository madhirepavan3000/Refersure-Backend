package com.referral.payments;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface RazorpayGateway {

    @Data
    @AllArgsConstructor
    class RazorpayOrder {
        private String id;
        private Long amountInPaise;
        private String currency;
    }

    RazorpayOrder createOrder(Long amountInPaise, String currency, String receiptId);

    boolean verifySignature(String payload, String providedSignature);
}


