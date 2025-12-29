package com.referral.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.referral.payments.dto.CreateOrderRequest;
import com.referral.payments.dto.CreateOrderResponse;
import com.referral.quota.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayGateway razorpayGateway;
    private final QuotaService quotaService;
    private final ObjectMapper objectMapper;

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amountInPaise(request.getAmountInPaise())
                .currency(request.getCurrency())
                .quotaUnits(request.getQuotaUnits())
                .status(PaymentStatus.CREATED)
                .build();

        Payment saved = paymentRepository.save(payment);

        RazorpayGateway.RazorpayOrder order = razorpayGateway.createOrder(
                saved.getAmountInPaise(),
                saved.getCurrency(),
                "receipt_" + saved.getId()
        );

        saved.setRazorpayOrderId(order.getId());
        saved.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(saved);

        return new CreateOrderResponse(order.getId(), order.getCurrency(), order.getAmountInPaise(), razorpayKeyId);
    }

    @Transactional
    public void handleWebhook(String signature, String payload) {
        if (!razorpayGateway.verifySignature(payload, signature)) {
            throw new IllegalArgumentException("Invalid Razorpay webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();
            JsonNode payloadPayment = root.path("payload").path("payment").path("entity");

            String razorpayPaymentId = payloadPayment.path("id").asText();
            String razorpayOrderId = payloadPayment.path("order_id").asText();
            String status = payloadPayment.path("status").asText();

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for order id " + razorpayOrderId));

            payment.setRazorpayPaymentId(razorpayPaymentId);

            if ("payment.captured".equals(event) || "captured".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.CAPTURED);
                quotaService.applyQuotaFromPayment(payment);
            } else if ("failed".equalsIgnoreCase(status) || event.startsWith("payment.failed")) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment failed");
            }

            paymentRepository.save(payment);

            log.info("Processed Razorpay webhook event={} for paymentId={}, status={}",
                    event, payment.getId(), payment.getStatus());
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            throw new IllegalArgumentException("Invalid webhook payload");
        }
    }
}


