package com.referral.referral;

public interface ReferralEventPublisher {

    void publishReferralCreated(Referral referral);

    void publishReferralStatusChanged(Referral referral);
}


