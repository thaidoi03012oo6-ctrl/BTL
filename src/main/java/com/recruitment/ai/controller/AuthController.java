package com.recruitment.ai.controller;

import com.recruitment.ai.dto.request.LoginRequest;
import com.recruitment.ai.dto.request.RegisterCandidateRequest;
import com.recruitment.ai.dto.request.RegisterRecruiterRequest;
import com.recruitment.ai.dto.response.AuthResponse;
import com.recruitment.ai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/candidate")
    public ResponseEntity<AuthResponse> registerCandidate(@Valid @RequestBody RegisterCandidateRequest request) {
        return ResponseEntity.ok(authService.registerCandidate(request));
    }

    @PostMapping("/register/recruiter")
    public ResponseEntity<AuthResponse> registerRecruiter(@Valid @RequestBody RegisterRecruiterRequest request) {
        return ResponseEntity.ok(authService.registerRecruiter(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Chưa đăng nhập"));
        }
        java.util.UUID userId = java.util.UUID.fromString(authentication.getName());
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            @RequestBody java.util.Map<String, Object> body,
            org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Chưa đăng nhập"));
        }
        java.util.UUID userId = java.util.UUID.fromString(authentication.getName());
        return ResponseEntity.ok(authService.updateProfile(userId, body));
    }
}
