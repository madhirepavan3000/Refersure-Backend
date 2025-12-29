package com.referral.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReferralRepository extends JpaRepository<Referral, Long> {

    List<Referral> findByReferrerUserId(Long referrerUserId);

    List<Referral> findByStatusAndSlaDueAtBefore(ReferralStatus status, Instant cutoff);

    List<Referral> findByStatusAndRetryCountLessThanAndLastAttemptAtBefore(
            ReferralStatus status,
            int maxRetries,
            Instant cutoff
    );
}


