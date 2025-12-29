package com.referral.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AwsNotificationService implements NotificationService {

    @Override
    public void sendVerificationOtp(String email, String otp) {
        log.info("Sending verification OTP to {} (stub) otp={}", email, otp);
        // In a real implementation, publish email via AWS SES and/or SMS via SNS with rate limiting.
    }
}


