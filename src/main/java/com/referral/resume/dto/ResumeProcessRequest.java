package com.referral.resume.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResumeProcessRequest {

    @NotNull
    private Long referralId;

    @Email
    @NotBlank
    private String candidateEmail;

    /**
     * Either raw resume text or a URL to a stored resume (S3, etc.).
     * For this skeleton we treat it as raw text.
     */
    @NotBlank
    private String resumeContent;
}


