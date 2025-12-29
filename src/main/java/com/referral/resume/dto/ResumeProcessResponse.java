package com.referral.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumeProcessResponse {

    private Long analysisId;
    private Double score;
    private boolean duplicate;
    private Long duplicateOfAnalysisId;
}


