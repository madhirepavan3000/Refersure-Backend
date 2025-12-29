package com.referral.referral.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReferralCreateRequest {

    @NotBlank
    private String candidateName;

    @Email
    @NotBlank
    private String candidateEmail;

    private String candidateResumeUrl;

    @NotNull
    private Long referrerUserId;
}


