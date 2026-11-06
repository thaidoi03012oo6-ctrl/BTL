package com.recruitment.ai.controller;

import com.recruitment.ai.dto.response.CvSearchResultDto;
import com.recruitment.ai.entity.Candidate;
import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.repository.ApplyJobRepository;
import com.recruitment.ai.repository.CandidateRepository;
import com.recruitment.ai.repository.CvRepository;
import com.recruitment.ai.repository.SuggestedMatchRepository;
import com.recruitment.ai.service.CvProcessingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cvs")
public class CvController {

    private final CvProcessingService cvProcessingService;
    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;
    private final ApplyJobRepository applyJobRepository;
    private final SuggestedMatchRepository suggestedMatchRepository;

    public CvController(
            CvProcessingService cvProcessingService,
            CandidateRepository candidateRepository,
            CvRepository cvRepository,
            ApplyJobRepository applyJobRepository,
            SuggestedMatchRepository suggestedMatchRepository) {
        this.cvProcessingService = cvProcessingService;
        this.candidateRepository = candidateRepository;
        this.cvRepository = cvRepository;
        this.applyJobRepository = applyJobRepository;
        this.suggestedMatchRepository = suggestedMatchRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Cv> uploadCv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UUID candidateId = UUID.fromString(authentication.getName());
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên!"));

        Cv savedCv = cvProcessingService.processAndSaveCvFromSinglePdf(file, candidate);
        return ResponseEntity.ok(savedCv);
    }

    @GetMapping("/my-cvs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<Cv>> getMyCvs(Authentication authentication) {
        UUID candidateId = UUID.fromString(authentication.getName());
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên!"));

        return ResponseEntity.ok(cvRepository.findByCandidate(candidate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CvSearchResultDto>> searchCvs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(defaultValue = "10") int topK) {

        List<CvSearchResultDto> results = cvProcessingService.searchCvs(keyword, domain, khuVuc, topK);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cv> getCvById(@PathVariable UUID id) {
        return cvRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<?> updateCv(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Cv cv = cvRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CV: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = UUID.fromString(authentication.getName());

        if (!isAdmin && !cv.getCandidate().getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền chỉnh sửa CV này"));
        }

        if (body.containsKey("cvTitle")) cv.setCvTitle(body.get("cvTitle"));
        if (body.containsKey("domain")) cv.setDomain(body.get("domain"));
        if (body.containsKey("experience")) cv.setExperience(body.get("experience"));
        if (body.containsKey("khuVuc")) cv.setKhuVuc(body.get("khuVuc"));

        cvRepository.save(cv);
        return ResponseEntity.ok(Map.of("message", "Cập nhật CV thành công", "cvId", cv.getCvId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteCv(@PathVariable UUID id, Authentication authentication) {
        Cv cv = cvRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CV: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = UUID.fromString(authentication.getName());

        if (!isAdmin && !cv.getCandidate().getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xóa CV này"));
        }

        applyJobRepository.deleteByCv(cv);
        suggestedMatchRepository.deleteByCv(cv);
        cvRepository.delete(cv);

        return ResponseEntity.ok(Map.of("message", "Xóa CV thành công"));
    }
}
