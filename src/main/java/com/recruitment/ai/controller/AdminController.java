package com.recruitment.ai.controller;

import com.recruitment.ai.config.DataInitializer;
import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.User;
import com.recruitment.ai.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final CvRepository cvRepository;
    private final JdRepository jdRepository;
    private final SuggestedMatchRepository suggestedMatchRepository;
    private final ApplyJobRepository applyJobRepository;
    private final DataInitializer dataInitializer;

    public AdminController(
            UserRepository userRepository,
            CandidateRepository candidateRepository,
            RecruitmentRepository recruitmentRepository,
            CvRepository cvRepository,
            JdRepository jdRepository,
            SuggestedMatchRepository suggestedMatchRepository,
            ApplyJobRepository applyJobRepository,
            DataInitializer dataInitializer) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.cvRepository = cvRepository;
        this.jdRepository = jdRepository;
        this.suggestedMatchRepository = suggestedMatchRepository;
        this.applyJobRepository = applyJobRepository;
        this.dataInitializer = dataInitializer;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCandidates", candidateRepository.count());
        stats.put("totalRecruiters", recruitmentRepository.count());
        stats.put("totalCvs", cvRepository.count());
        stats.put("totalJds", jdRepository.count());
        stats.put("totalMatches", suggestedMatchRepository.count());
        stats.put("totalApplications", applyJobRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/cvs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Cv>> getAllCvs() {
        return ResponseEntity.ok(cvRepository.findAll());
    }

    @PutMapping("/cvs/{cvId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCv(@PathVariable UUID cvId, @RequestBody Map<String, String> body) {
        Cv cv = cvRepository.findById(cvId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CV"));
        if (body.containsKey("cvTitle")) cv.setCvTitle(body.get("cvTitle"));
        if (body.containsKey("domain")) cv.setDomain(body.get("domain"));
        if (body.containsKey("experience")) cv.setExperience(body.get("experience"));
        if (body.containsKey("khuVuc")) cv.setKhuVuc(body.get("khuVuc"));
        cvRepository.save(cv);
        return ResponseEntity.ok(Map.of("message", "Cập nhật CV thành công"));
    }

    @DeleteMapping("/cvs/{cvId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCv(@PathVariable UUID cvId) {
        if (!cvRepository.existsById(cvId)) {
            return ResponseEntity.notFound().build();
        }
        cvRepository.deleteById(cvId);
        return ResponseEntity.ok(Map.of("message", "Xóa CV thành công"));
    }

    @GetMapping("/jds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Jd>> getAllJds() {
        return ResponseEntity.ok(jdRepository.findAll());
    }

    @PutMapping("/jds/{jdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateJd(@PathVariable UUID jdId, @RequestBody Map<String, String> body) {
        Jd jd = jdRepository.findById(jdId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD"));
        if (body.containsKey("jobTitle")) jd.setJobTitle(body.get("jobTitle"));
        if (body.containsKey("domain")) jd.setDomain(body.get("domain"));
        if (body.containsKey("experience")) jd.setExperience(body.get("experience"));
        if (body.containsKey("khuVuc")) jd.setKhuVuc(body.get("khuVuc"));
        if (body.containsKey("status")) jd.setStatus(body.get("status"));
        jdRepository.save(jd);
        return ResponseEntity.ok(Map.of("message", "Cập nhật JD thành công"));
    }

    @DeleteMapping("/jds/{jdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteJd(@PathVariable UUID jdId) {
        if (!jdRepository.existsById(jdId)) {
            return ResponseEntity.notFound().build();
        }
        jdRepository.deleteById(jdId);
        return ResponseEntity.ok(Map.of("message", "Xóa JD thành công"));
    }

    @PostMapping("/seed-samples")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> seedSamples() {
        dataInitializer.seedDiverseData();
        return ResponseEntity.ok(Map.of("message", "Đã thêm dữ liệu mẫu CV và JD vào database thành công!"));
    }
}
