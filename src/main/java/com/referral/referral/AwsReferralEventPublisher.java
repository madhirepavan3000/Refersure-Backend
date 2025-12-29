package com.referral.referral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwsReferralEventPublisher implements ReferralEventPublisher {

    // In a real implementation you would inject EventBridge / SQS clients here

    @Override
    public void publishReferralCreated(Referral referral) {
        log.info("Publishing referral created event for id={}", referral.getId());
    }

    @Override
    public void publishReferralStatusChanged(Referral referral) {
        log.info("Publishing referral status changed event for id={}, status={}",
                referral.getId(), referral.getStatus());
    }
}


