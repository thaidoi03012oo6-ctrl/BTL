package com.recruitment.ai.service;

import com.recruitment.ai.dto.request.LoginRequest;
import com.recruitment.ai.dto.request.RegisterCandidateRequest;
import com.recruitment.ai.dto.request.RegisterRecruiterRequest;
import com.recruitment.ai.dto.response.AuthResponse;
import com.recruitment.ai.entity.Candidate;
import com.recruitment.ai.entity.Recruitment;
import com.recruitment.ai.entity.User;
import com.recruitment.ai.repository.CandidateRepository;
import com.recruitment.ai.repository.RecruitmentRepository;
import com.recruitment.ai.repository.UserRepository;
import com.recruitment.ai.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            CandidateRepository candidateRepository,
            RecruitmentRepository recruitmentRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse registerCandidate(RegisterCandidateRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống!");
        }

        Candidate candidate = new Candidate();
        candidate.setEmail(req.getEmail());
        candidate.setPassword(passwordEncoder.encode(req.getPassword()));
        candidate.setRole("ROLE_CANDIDATE");
        candidate.setFullName(req.getFullName());
        candidate.setBirthDay(req.getBirthDay());
        candidate.setAddress(req.getAddress());
        candidate.setPhone(req.getPhone());
        candidate.setStatus("ACTIVE");
        candidate.setCreatedAt(Instant.now());
        candidate.setUpdatedAt(Instant.now());

        Candidate saved = candidateRepository.save(candidate);
        String token = tokenProvider.generateToken(saved.getUserId(), saved.getEmail(), saved.getRole());

        return new AuthResponse(token, "Bearer", saved.getUserId(), saved.getEmail(), saved.getRole(), saved.getFullName());
    }

    @Transactional
    public AuthResponse registerRecruiter(RegisterRecruiterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống!");
        }

        Recruitment recruitment = new Recruitment();
        recruitment.setEmail(req.getEmail());
        recruitment.setPassword(passwordEncoder.encode(req.getPassword()));
        recruitment.setRole("ROLE_RECRUITMENT");
        recruitment.setCompanyName(req.getCompanyName());
        recruitment.setPhone(req.getPhone());
        recruitment.setAddress(req.getAddress());
        recruitment.setContactName(req.getContactName());
        recruitment.setCreatedAt(Instant.now());
        recruitment.setUpdatedAt(Instant.now());

        Recruitment saved = recruitmentRepository.save(recruitment);
        String token = tokenProvider.generateToken(saved.getUserId(), saved.getEmail(), saved.getRole());

        return new AuthResponse(token, "Bearer", saved.getUserId(), saved.getEmail(), saved.getRole(), saved.getCompanyName());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác!");
        }

        String token = tokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        String displayName = user.getEmail();
        if ("ROLE_CANDIDATE".equals(user.getRole())) {
            displayName = candidateRepository.findById(user.getUserId())
                    .map(Candidate::getFullName)
                    .filter(s -> s != null && !s.isBlank())
                    .orElse(user.getEmail());
        } else if ("ROLE_RECRUITMENT".equals(user.getRole())) {
            displayName = recruitmentRepository.findById(user.getUserId())
                    .map(Recruitment::getCompanyName)
                    .filter(s -> s != null && !s.isBlank())
                    .orElse(user.getEmail());
        } else if ("ROLE_ADMIN".equals(user.getRole())) {
            displayName = "Quản trị viên";
        }

        return new AuthResponse(token, "Bearer", user.getUserId(), user.getEmail(), user.getRole(), displayName);
    }

    public java.util.Map<String, Object> getProfile(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", user.getUserId());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("createdAt", user.getCreatedAt());

        if ("ROLE_CANDIDATE".equals(user.getRole())) {
            candidateRepository.findById(userId).ifPresent(c -> {
                map.put("fullName", c.getFullName());
                map.put("displayName", c.getFullName() != null && !c.getFullName().isBlank() ? c.getFullName() : user.getEmail());
                map.put("phone", c.getPhone());
                map.put("address", c.getAddress());
                map.put("birthDay", c.getBirthDay());
                map.put("status", c.getStatus());
            });
        } else if ("ROLE_RECRUITMENT".equals(user.getRole())) {
            recruitmentRepository.findById(userId).ifPresent(r -> {
                map.put("companyName", r.getCompanyName());
                map.put("displayName", r.getCompanyName() != null && !r.getCompanyName().isBlank() ? r.getCompanyName() : user.getEmail());
                map.put("contactName", r.getContactName());
                map.put("phone", r.getPhone());
                map.put("address", r.getAddress());
            });
        } else if ("ROLE_ADMIN".equals(user.getRole())) {
            map.put("displayName", "Quản trị viên");
        }
        return map;
    }

    @Transactional
    public java.util.Map<String, Object> updateProfile(java.util.UUID userId, java.util.Map<String, Object> req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        user.setUpdatedAt(Instant.now());

        if ("ROLE_CANDIDATE".equals(user.getRole())) {
            candidateRepository.findById(userId).ifPresent(c -> {
                if (req.containsKey("fullName") && req.get("fullName") != null) c.setFullName(req.get("fullName").toString());
                if (req.containsKey("phone")) c.setPhone(req.get("phone") != null ? req.get("phone").toString() : null);
                if (req.containsKey("address")) c.setAddress(req.get("address") != null ? req.get("address").toString() : null);
                if (req.containsKey("birthDay") && req.get("birthDay") != null && !req.get("birthDay").toString().isBlank()) {
                    try {
                        c.setBirthDay(java.time.LocalDate.parse(req.get("birthDay").toString()));
                    } catch (Exception ignored) {}
                }
                candidateRepository.save(c);
            });
        } else if ("ROLE_RECRUITMENT".equals(user.getRole())) {
            recruitmentRepository.findById(userId).ifPresent(r -> {
                if (req.containsKey("companyName") && req.get("companyName") != null) r.setCompanyName(req.get("companyName").toString());
                if (req.containsKey("contactName")) r.setContactName(req.get("contactName") != null ? req.get("contactName").toString() : null);
                if (req.containsKey("phone")) r.setPhone(req.get("phone") != null ? req.get("phone").toString() : null);
                if (req.containsKey("address")) r.setAddress(req.get("address") != null ? req.get("address").toString() : null);
                recruitmentRepository.save(r);
            });
        }
        userRepository.save(user);
        return getProfile(userId);
    }
}
