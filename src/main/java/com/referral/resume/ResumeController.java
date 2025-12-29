package com.referral.resume;

import com.referral.resume.dto.ResumeProcessRequest;
import com.referral.resume.dto.ResumeProcessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeProcessingService resumeProcessingService;

    @PostMapping("/process")
    public ResponseEntity<ResumeProcessResponse> process(@Valid @RequestBody ResumeProcessRequest request) {
        return ResponseEntity.ok(resumeProcessingService.parseAndScore(request));
    }
}


