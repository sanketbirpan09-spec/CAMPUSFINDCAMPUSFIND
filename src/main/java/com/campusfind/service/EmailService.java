package com.campusfind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        @Value("${app.mail.enabled:false}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
    }

    public void sendPasswordResetEmail(String toEmail, String rawToken, String resetUrl) {
        if (mailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("CampusFind — Password Reset Request");
                message.setText("Hello,\n\n" +
                        "A password reset was requested for your CampusFind account.\n" +
                        "Use the following link to reset your password:\n\n" +
                        resetUrl + "\n\n" +
                        "This link will expire in 15 minutes.\n" +
                        "If you did not request this reset, please ignore this email.\n\n" +
                        "— CampusFind Administration");
                mailSender.send(message);
                logger.info("Password reset email sent to {}", toEmail);
                return;
            } catch (Exception e) {
                logger.warn("Could not deliver SMTP email to {}: {}. Falling back to local logging.", toEmail, e.getMessage());
            }
        }

        // Local development fallback
        logger.info("==================================================================");
        logger.info("[CAMPUSFIND LOCAL-DEV EMAIL SIMULATOR]");
        logger.info("To: {}", toEmail);
        logger.info("Subject: Password Reset Request");
        logger.info("Reset Token: {}", rawToken);
        logger.info("Password Reset Link: {}", resetUrl);
        logger.info("==================================================================");
    }
}
