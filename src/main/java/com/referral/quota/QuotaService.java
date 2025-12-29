package com.referral.quota;

import com.referral.payments.Payment;

public interface QuotaService {

    void applyQuotaFromPayment(Payment payment);

    /**
     * Hook that would be invoked by AWS Scheduler to refresh monthly quotas.
     */
    void refreshMonthlyQuota(Long userId);
}


