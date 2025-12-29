package com.referral.resume;

import com.referral.resume.dto.ResumeProcessRequest;
import com.referral.resume.dto.ResumeProcessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiResumeProcessingService implements ResumeProcessingService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Value("${openai.api-key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4-turbo-preview}")
    private String model;

    @Override
    public ResumeProcessResponse parseAndScore(ResumeProcessRequest request) {
        // In a real implementation, you would:
        // 1. Call OpenAI with a carefully designed prompt to extract structured fields from resumeContent.
        // 2. Use the model to compute a suitability score based on role, skills, experience.
        // 3. Perform semantic similarity / embedding-based duplicate detection against previous resumes.

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("OpenAI API key not configured, using stubbed resume scoring");
        }

        // Stub: compute a very naive score based on length.
        double score = Math.min(100.0, request.getResumeContent().length() / 100.0);

        // Naive duplicate detection: check if same candidateEmail has any previous analyses.
        List<ResumeAnalysis> previous = resumeAnalysisRepository.findByCandidateEmail(request.getCandidateEmail());
        Optional<ResumeAnalysis> latest = previous.stream()
                .max(Comparator.comparing(ResumeAnalysis::getCreatedAt));

        boolean duplicate = latest.isPresent();
        Long duplicateOfId = latest.map(ResumeAnalysis::getId).orElse(null);

        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .referralId(request.getReferralId())
                .candidateEmail(request.getCandidateEmail())
                .parsedText(request.getResumeContent())
                .score(score)
                .duplicateOfAnalysisId(duplicateOfId)
                .build();

        ResumeAnalysis saved = resumeAnalysisRepository.save(analysis);

        return new ResumeProcessResponse(saved.getId(), saved.getScore(), duplicate, duplicateOfId);
    }
}


