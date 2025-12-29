package com.referral.referral;

import com.referral.referral.dto.ReferralCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ReferralEventPublisher eventPublisher;

    @Value("${referral.sla.hours:24}")
    private long slaHours;

    @Value("${referral.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${referral.retry.backoff-millis:5000}")
    private long backoffMillis;

    @Transactional
    public Referral createReferral(ReferralCreateRequest request) {
        Referral referral = Referral.builder()
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .candidateResumeUrl(request.getCandidateResumeUrl())
                .referrerUserId(request.getReferrerUserId())
                .status(ReferralStatus.PENDING_VERIFICATION)
                .slaDueAt(Instant.now().plus(slaHours, ChronoUnit.HOURS))
                .retryCount(0)
                .maxRetries(maxRetryAttempts)
                .build();

        Referral saved = referralRepository.save(referral);
        eventPublisher.publishReferralCreated(saved);
        return saved;
    }

    public Referral getReferral(Long id) {
        return referralRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Referral not found"));
    }

    public List<Referral> getReferralsForReferrer(Long referrerUserId) {
        return referralRepository.findByReferrerUserId(referrerUserId);
    }

    @Transactional
    public void updateStatus(Long id, ReferralStatus status, String failureReason) {
        Referral referral = getReferral(id);
        referral.setStatus(status);
        if (failureReason != null) {
            referral.setFailureReason(failureReason);
        }
        referralRepository.save(referral);
        eventPublisher.publishReferralStatusChanged(referral);
    }

    /**
     * SLA monitoring hook.
     * This can be triggered by EventBridge scheduler or internal @Scheduled job.
     */
    @Scheduled(fixedDelayString = "${referral.sla.monitor-interval-millis:60000}")
    @Transactional
    public void checkSlaBreaches() {
        Instant now = Instant.now();
        List<Referral> breached = referralRepository.findByStatusAndSlaDueAtBefore(
                ReferralStatus.PENDING_VERIFICATION, now);
        for (Referral referral : breached) {
            log.warn("Referral id={} breached SLA, marking as EXPIRED", referral.getId());
            referral.setStatus(ReferralStatus.EXPIRED);
            referralRepository.save(referral);
            eventPublisher.publishReferralStatusChanged(referral);
        }
    }

    /**
     * Retry hook for transient failures.
     * This represents integration with AWS Lambda/EventBridge-based retries.
     */
    @Scheduled(fixedDelayString = "${referral.retry.monitor-interval-millis:60000}")
    @Transactional
    public void retryFailedReferrals() {
        Instant cutoff = Instant.now().minusMillis(backoffMillis);
        List<Referral> candidates = referralRepository
                .findByStatusAndRetryCountLessThanAndLastAttemptAtBefore(
                        ReferralStatus.FAILED, maxRetryAttempts, cutoff);

        for (Referral referral : candidates) {
            log.info("Retrying referral id={} (attempt {}/{})",
                    referral.getId(), referral.getRetryCount() + 1, maxRetryAttempts);

            referral.setRetryCount(referral.getRetryCount() + 1);
            referral.setLastAttemptAt(Instant.now());
            referralRepository.save(referral);

            // In a real implementation we would enqueue a message to SQS or invoke a Lambda
            eventPublisher.publishReferralStatusChanged(referral);
        }
    }
}


