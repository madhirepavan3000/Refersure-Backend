package com.referral.resume;

import com.referral.resume.dto.ResumeProcessRequest;
import com.referral.resume.dto.ResumeProcessResponse;

public interface ResumeProcessingService {

    ResumeProcessResponse parseAndScore(ResumeProcessRequest request);
}


