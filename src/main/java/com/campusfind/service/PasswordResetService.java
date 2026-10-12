package com.campusfind.service;

import com.campusfind.dto.ForgotPasswordRequest;
import com.campusfind.dto.ResetPasswordRequest;
import com.campusfind.exception.BadRequestException;
import com.campusfind.model.PasswordResetToken;
import com.campusfind.model.User;
import com.campusfind.repository.PasswordResetTokenRepository;
import com.campusfind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final boolean logResetTokens;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                @Value("${app.dev-mode.log-reset-tokens:true}") boolean logResetTokens) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.logResetTokens = logResetTokens;
    }

    public String createPasswordResetToken(ForgotPasswordRequest request, String baseUrl) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No registered account found with that email address"));

        // Generate cryptographically secure 32-byte raw token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = HexFormat.of().formatHex(randomBytes);

        // Hash token with SHA-256 before persisting
        String tokenHash = hashToken(rawToken);

        // Invalidate previous tokens
        tokenRepository.invalidateExistingTokensForUser(user.getId());

        // Save token to database with 15-minute expiration
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Construct reset link
        String resetUrl = baseUrl + "/reset-password.html?token=" + rawToken;

        // Deliver email or log locally
        emailService.sendPasswordResetEmail(user.getEmail(), rawToken, resetUrl);

        return rawToken;
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        String rawToken = request.getToken().trim();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (token.isExpired() || token.isUsed()) {
            throw new BadRequestException("This reset link has expired or has already been used");
        }

        // Hash new password using BCrypt
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(token.getUserId(), newPasswordHash);

        // Mark single-use token as used
        tokenRepository.markUsed(token.getId());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
