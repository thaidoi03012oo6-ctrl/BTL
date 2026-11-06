package com.recruitment.ai.controller;

import com.recruitment.ai.dto.response.JdMatchEvaluationDto;
import com.recruitment.ai.dto.response.MatchEvaluationDto;
import com.recruitment.ai.service.MatchingService;
import com.recruitment.ai.service.MatchingService.CvVectorItem;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/jd/{jdId}")
    @PreAuthorize("hasAnyRole('RECRUITMENT', 'ADMIN')")
    public ResponseEntity<List<MatchEvaluationDto>> matchCvsForJob(
            @PathVariable UUID jdId,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) String domain) {
        return ResponseEntity.ok(matchingService.matchCvsForJd(jdId, topK, domain));
    }

    @GetMapping("/search-cvs")
    @PreAuthorize("hasAnyRole('RECRUITMENT', 'ADMIN')")
    public ResponseEntity<List<CvVectorItem>> searchCvsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(required = false) String domain) {
        return ResponseEntity.ok(matchingService.searchCvsByKeyword(keyword, topK, domain));
    }

    @GetMapping("/cv/{cvId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<List<JdMatchEvaluationDto>> matchJdsForCv(
            @PathVariable UUID cvId,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) String domain) {
        return ResponseEntity.ok(matchingService.matchJdsForCv(cvId, topK, domain));
    }
}
