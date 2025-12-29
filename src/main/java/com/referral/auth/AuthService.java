package com.referral.auth;

import com.referral.auth.dto.AuthRequest;
import com.referral.auth.dto.AuthResponse;
import com.referral.auth.dto.OtpVerifyRequest;
import com.referral.auth.dto.RefreshTokenRequest;
import com.referral.auth.dto.RegisterRequest;
import com.referral.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final com.referral.security.JwtTokenProvider jwtTokenProvider;
    private final NotificationService notificationService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void register(RegisterRequest request) {
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserAccount user = UserAccount.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .build();
        user.getRoles().add(UserRole.ROLE_CANDIDATE);

        // Generate OTP for email verification
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiresAt(Instant.now().plusSeconds(10 * 60));

        userAccountRepository.save(user);

        notificationService.sendVerificationOtp(user.getEmail(), otp);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void verifyOtp(OtpVerifyRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null ||
                Instant.now().isAfter(user.getOtpExpiresAt())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);

        userAccountRepository.save(user);
    }

    private String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}


