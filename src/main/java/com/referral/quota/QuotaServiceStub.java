package com.referral.quota;

import com.referral.payments.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QuotaServiceStub implements QuotaService {

    @Override
    public void applyQuotaFromPayment(Payment payment) {
        log.info("Applying {} quota units for userId={} from paymentId={}",
                payment.getQuotaUnits(), payment.getUserId(), payment.getId());
        // In a real implementation, increment the user's quota balance in DynamoDB / relational DB.
    }

    @Override
    public void refreshMonthlyQuota(Long userId) {
        log.info("Refreshing monthly quota for userId={}", userId);
        // In a real implementation, AWS Scheduler would invoke an internal API that calls this method.
    }
}


