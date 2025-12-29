package com.referral.referral;

import com.referral.referral.dto.ReferralCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    public ResponseEntity<Referral> createReferral(@Valid @RequestBody ReferralCreateRequest request) {
        return ResponseEntity.ok(referralService.createReferral(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Referral> getReferral(@PathVariable Long id) {
        return ResponseEntity.ok(referralService.getReferral(id));
    }

    @GetMapping
    public ResponseEntity<List<Referral>> getReferralsForReferrer(@RequestParam Long referrerUserId) {
        return ResponseEntity.ok(referralService.getReferralsForReferrer(referrerUserId));
    }
}


