package com.campusfind.controller;

import com.campusfind.dto.ApiResponse;
import com.campusfind.dto.LoginRequest;
import com.campusfind.dto.RegisterRequest;
import com.campusfind.dto.UserResponse;
import com.campusfind.model.User;
import com.campusfind.service.TokenRegistryService;
import com.campusfind.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenRegistryService tokenRegistryService;

    public AuthController(UserService userService, TokenRegistryService tokenRegistryService) {
        this.userService = userService;
        this.tokenRegistryService = tokenRegistryService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletRequest httpRequest) {
        User user = userService.registerUser(request);

        // Automatically log in after successful registration
        authenticateSession(user, httpRequest);

        // Generate cross-origin auth token
        String token = tokenRegistryService.generateToken(user);

        return ResponseEntity.ok(ApiResponse.success(
                "Registration successful! Welcome to CampusFind.",
                UserResponse.fromUser(user, token)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest) {
        User user = userService.authenticate(request);

        authenticateSession(user, httpRequest);

        // Generate cross-origin auth token
        String token = tokenRegistryService.generateToken(user);

        return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                UserResponse.fromUser(user, token)
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        // Invalidate session if present
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Remove token from registry if passed in header
        String token = httpRequest.getHeader("X-Auth-Token");
        if (token == null || token.isBlank()) {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }
        if (token != null) {
            tokenRegistryService.removeToken(token);
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Not authenticated", null));
        }

        String email = auth.getName();
        return userService.findByEmail(email)
                .map(user -> {
                    String token = httpRequest.getHeader("X-Auth-Token");
                    return ResponseEntity.ok(ApiResponse.success("User profile", UserResponse.fromUser(user, token)));
                })
                .orElse(ResponseEntity.ok(new ApiResponse<>(false, "User not found", null)));
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(csrfToken);
    }

    private void authenticateSession(User user, HttpServletRequest httpRequest) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(authority)
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }
}
