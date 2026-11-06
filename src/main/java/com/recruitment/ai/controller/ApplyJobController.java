package com.recruitment.ai.controller;

import com.recruitment.ai.dto.request.ApplyJobRequest;
import com.recruitment.ai.dto.response.ApplyJobDto;
import com.recruitment.ai.entity.*;
import com.recruitment.ai.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/apply")
public class ApplyJobController {

    private final ApplyJobRepository applyJobRepository;
    private final CvRepository cvRepository;
    private final JdRepository jdRepository;
    private final CandidateRepository candidateRepository;
    private final RecruitmentRepository recruitmentRepository;

    public ApplyJobController(
            ApplyJobRepository applyJobRepository,
            CvRepository cvRepository,
            JdRepository jdRepository,
            CandidateRepository candidateRepository,
            RecruitmentRepository recruitmentRepository) {
        this.applyJobRepository = applyJobRepository;
        this.cvRepository = cvRepository;
        this.jdRepository = jdRepository;
        this.candidateRepository = candidateRepository;
        this.recruitmentRepository = recruitmentRepository;
    }

    @PostMapping
    public ResponseEntity<?> applyJob(@RequestBody ApplyJobRequest request, Authentication auth) {
        UUID candidateId = UUID.fromString(auth.getName());
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên"));

        Cv cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CV"));

        if (!cv.getCandidate().getUserId().equals(candidate.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "CV không thuộc về bạn"));
        }

        Jd jd = jdRepository.findById(request.getJdId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD"));

        Optional<ApplyJob> existing = applyJobRepository.findByCvAndJd(cv, jd);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã ứng tuyển vào vị trí này rồi"));
        }

        ApplyJob apply = new ApplyJob();
        apply.setCv(cv);
        apply.setJd(jd);
        apply.setStatus("PENDING");
        applyJobRepository.save(apply);

        return ResponseEntity.ok(Map.of("message", "Ứng tuyển thành công", "applyId", apply.getApplyId()));
    }

    @GetMapping("/candidate")
    public ResponseEntity<List<ApplyJobDto>> getCandidateApplications(Authentication auth) {
        UUID candidateId = UUID.fromString(auth.getName());
        List<ApplyJob> applies = applyJobRepository.findByCandidateId(candidateId);
        return ResponseEntity.ok(applies.stream().map(this::toDto).toList());
    }

    @GetMapping("/recruiter")
    public ResponseEntity<List<ApplyJobDto>> getRecruiterApplications(Authentication auth) {
        UUID recruiterId = UUID.fromString(auth.getName());
        List<ApplyJob> applies = applyJobRepository.findByRecruiterId(recruiterId);
        return ResponseEntity.ok(applies.stream().map(this::toDto).toList());
    }

    @PutMapping("/{applyId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID applyId, @RequestBody Map<String, String> body, Authentication auth) {
        ApplyJob apply = applyJobRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển"));

        UUID recruiterId = UUID.fromString(auth.getName());
        if (!apply.getJd().getRecruitment().getUserId().equals(recruiterId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền cập nhật đơn này"));
        }

        String status = body.get("status");
        if (!"ACCEPTED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ"));
        }

        apply.setStatus(status);
        applyJobRepository.save(apply);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công", "status", status));
    }

    private ApplyJobDto toDto(ApplyJob a) {
        ApplyJobDto dto = new ApplyJobDto();
        dto.setApplyId(a.getApplyId());
        dto.setCvId(a.getCv().getCvId());
        dto.setCvTitle(a.getCv().getCvTitle());
        dto.setCandidateName(a.getCv().getCandidate().getFullName());
        dto.setJdId(a.getJd().getJdId());
        dto.setJobTitle(a.getJd().getJobTitle());
        dto.setCompanyName(a.getJd().getRecruitment().getCompanyName());
        dto.setStatus(a.getStatus());
        dto.setApplyTime(a.getApplyTime());
        dto.setMatchScore(a.getMatchScore());
        return dto;
    }
}
