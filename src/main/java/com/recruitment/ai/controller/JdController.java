package com.recruitment.ai.controller;

import com.recruitment.ai.dto.response.JdSearchResultDto;
import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.Recruitment;
import com.recruitment.ai.repository.ApplyJobRepository;
import com.recruitment.ai.repository.JdRepository;
import com.recruitment.ai.repository.RecruitmentRepository;
import com.recruitment.ai.repository.SuggestedMatchRepository;
import com.recruitment.ai.service.JdProcessingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jds")
public class JdController {

    private final JdProcessingService jdProcessingService;
    private final RecruitmentRepository recruitmentRepository;
    private final JdRepository jdRepository;
    private final ApplyJobRepository applyJobRepository;
    private final SuggestedMatchRepository suggestedMatchRepository;

    public JdController(
            JdProcessingService jdProcessingService,
            RecruitmentRepository recruitmentRepository,
            JdRepository jdRepository,
            ApplyJobRepository applyJobRepository,
            SuggestedMatchRepository suggestedMatchRepository) {
        this.jdProcessingService = jdProcessingService;
        this.recruitmentRepository = recruitmentRepository;
        this.jdRepository = jdRepository;
        this.applyJobRepository = applyJobRepository;
        this.suggestedMatchRepository = suggestedMatchRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RECRUITMENT')")
    public ResponseEntity<Jd> uploadJd(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UUID recruiterId = UUID.fromString(authentication.getName());
        Recruitment recruitment = recruitmentRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Nhà tuyển dụng!"));

        Jd savedJd = jdProcessingService.processAndSaveJdFromSinglePdf(file, recruitment);
        return ResponseEntity.ok(savedJd);
    }

    @GetMapping("/my-jds")
    @PreAuthorize("hasRole('RECRUITMENT')")
    public ResponseEntity<List<Jd>> getMyJds(Authentication authentication) {
        UUID recruiterId = UUID.fromString(authentication.getName());
        Recruitment recruitment = recruitmentRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Nhà tuyển dụng!"));

        return ResponseEntity.ok(jdRepository.findByRecruitment(recruitment));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JdSearchResultDto>> searchJds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(defaultValue = "10") int topK) {

        List<JdSearchResultDto> results = jdProcessingService.searchJds(keyword, domain, khuVuc, topK);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jd> getJdById(@PathVariable UUID id) {
        return jdRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITMENT', 'ADMIN')")
    public ResponseEntity<?> updateJd(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Jd jd = jdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = UUID.fromString(authentication.getName());

        if (!isAdmin && !jd.getRecruitment().getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền chỉnh sửa JD này"));
        }

        if (body.containsKey("jobTitle")) jd.setJobTitle(body.get("jobTitle"));
        if (body.containsKey("domain")) jd.setDomain(body.get("domain"));
        if (body.containsKey("experience")) jd.setExperience(body.get("experience"));
        if (body.containsKey("khuVuc")) jd.setKhuVuc(body.get("khuVuc"));
        if (body.containsKey("status")) jd.setStatus(body.get("status"));
        if (body.containsKey("minSalary") && body.get("minSalary") != null && !body.get("minSalary").isBlank()) {
            try { jd.setMinSalary(new BigDecimal(body.get("minSalary"))); } catch (Exception ignored) {}
        }
        if (body.containsKey("maxSalary") && body.get("maxSalary") != null && !body.get("maxSalary").isBlank()) {
            try { jd.setMaxSalary(new BigDecimal(body.get("maxSalary"))); } catch (Exception ignored) {}
        }

        jdRepository.save(jd);
        return ResponseEntity.ok(Map.of("message", "Cập nhật JD thành công", "jdId", jd.getJdId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITMENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteJd(@PathVariable UUID id, Authentication authentication) {
        Jd jd = jdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = UUID.fromString(authentication.getName());

        if (!isAdmin && !jd.getRecruitment().getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xóa JD này"));
        }

        applyJobRepository.deleteByJd(jd);
        suggestedMatchRepository.deleteByJd(jd);
        jdRepository.delete(jd);

        return ResponseEntity.ok(Map.of("message", "Xóa JD thành công"));
    }
}
