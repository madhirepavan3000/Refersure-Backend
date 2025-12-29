package com.referral.payments;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class RazorpayGatewayStub implements RazorpayGateway {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    @Override
    public RazorpayOrder createOrder(Long amountInPaise, String currency, String receiptId) {
        // In a real implementation, this would call Razorpay's Orders API via razorpay-java client.
        String orderId = "order_" + UUID.randomUUID();
        log.info("Stub create Razorpay order id={} amount={} {}", orderId, amountInPaise, currency);
        return new RazorpayOrder(orderId, amountInPaise, currency);
    }

    @Override
    public boolean verifySignature(String payload, String providedSignature) {
        try {
            if (webhookSecret == null || webhookSecret.isBlank()) {
                log.warn("Razorpay webhook secret not configured, skipping signature verification");
                return true;
            }
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Hex.encodeHexString(hmac);
            boolean match = expectedSignature.equals(providedSignature);
            if (!match) {
                log.warn("Razorpay signature mismatch. expected={}, provided={}", expectedSignature, providedSignature);
            }
            return match;
        } catch (Exception e) {
            log.error("Error verifying Razorpay webhook signature", e);
            return false;
        }
    }
}


