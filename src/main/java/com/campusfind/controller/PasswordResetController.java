package com.campusfind.controller;

import com.campusfind.dto.ApiResponse;
import com.campusfind.dto.ForgotPasswordRequest;
import com.campusfind.dto.ResetPasswordRequest;
import com.campusfind.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final boolean logResetTokens;

    public PasswordResetController(PasswordResetService passwordResetService,
                                   @Value("${app.dev-mode.log-reset-tokens:true}") boolean logResetTokens) {
        this.passwordResetService = passwordResetService;
        this.logResetTokens = logResetTokens;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                                           HttpServletRequest httpRequest) {
        String scheme = httpRequest.getScheme();
        String serverName = httpRequest.getServerName();
        int serverPort = httpRequest.getServerPort();
        String baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);

        String rawToken = passwordResetService.createPasswordResetToken(request, baseUrl);

        Map<String, String> data = new HashMap<>();
        data.put("email", request.getEmail());
        if (logResetTokens) {
            data.put("devResetToken", rawToken);
            data.put("devResetUrl", baseUrl + "/reset-password.html?token=" + rawToken);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Password reset instructions have been generated. Please check your email or server log for the reset link.",
                data
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully! You can now log in with your new password."));
    }
}
