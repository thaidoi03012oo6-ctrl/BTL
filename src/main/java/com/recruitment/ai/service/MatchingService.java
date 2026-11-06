package com.recruitment.ai.service;

import com.recruitment.ai.dto.response.JdMatchEvaluationDto;
import com.recruitment.ai.dto.response.MatchEvaluationDto;
import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.SuggestedMatch;
import com.recruitment.ai.repository.CvRepository;
import com.recruitment.ai.repository.JdRepository;
import com.recruitment.ai.repository.SuggestedMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final JdRepository jdRepository;
    private final CvRepository cvRepository;
    private final EmbeddingHelperService embeddingHelper;
    private final GeminiEvaluationService geminiEvaluationService;
    private final SuggestedMatchRepository suggestedMatchRepository;

    public MatchingService(
            JdRepository jdRepository,
            CvRepository cvRepository,
            EmbeddingHelperService embeddingHelper,
            GeminiEvaluationService geminiEvaluationService,
            SuggestedMatchRepository suggestedMatchRepository) {
        this.jdRepository = jdRepository;
        this.cvRepository = cvRepository;
        this.embeddingHelper = embeddingHelper;
        this.geminiEvaluationService = geminiEvaluationService;
        this.suggestedMatchRepository = suggestedMatchRepository;
    }

    public record CvVectorItem(UUID cvId, String cvTitle, String candidateName, String domain, String rawText, double similarityScore) {}
    public record JdVectorItem(UUID jdId, String jobTitle, String companyName, String domain, String rawText, double similarityScore) {}

    @Transactional
    public List<MatchEvaluationDto> matchCvsForJd(UUID jdId, int topK, String domainFilter) {
        log.info("Matching CVs cho JD: {}, TopK: {}", jdId, topK);

        Jd jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD: " + jdId));

        String jdVector = jd.getVector();
        if (jdVector == null || jdVector.isBlank()) {
            jdVector = embeddingHelper.embedToPgVectorString(jd.getJobTitle() + " " + jd.getDescription());
            jd.setVector(jdVector);
            jdRepository.save(jd);
        }

        List<Object[]> rawCandidates = cvRepository.findTopKCvsByVectorSimilarity(jdVector, topK, domainFilter);
        List<MatchEvaluationDto> evaluations = new ArrayList<>();

        for (Object[] row : rawCandidates) {
            UUID cvId = (UUID) row[0];
            String cvTitle = (String) row[1];
            String candidateName = (String) row[2];
            String domain = (String) row[3];
            String rawCvText = (String) row[4];
            double vectorSim = ((Number) row[5]).doubleValue();

            GeminiEvaluationService.EvaluationResult eval = geminiEvaluationService.evaluateMatch(
                    jd.getJobTitle(),
                    jd.getDescription(),
                    cvTitle,
                    rawCvText
            );

            MatchEvaluationDto dto = new MatchEvaluationDto(
                    cvId,
                    candidateName,
                    cvTitle,
                    domain,
                    vectorSim,
                    eval.matchScore(),
                    eval.detailedReason(),
                    eval.skillGaps(),
                    eval.recommendations()
            );
            evaluations.add(dto);

            Cv cv = cvRepository.findById(cvId).orElse(null);
            if (cv != null) {
                SuggestedMatch suggestedMatch = suggestedMatchRepository.findByCvAndJd(cv, jd)
                        .orElseGet(() -> {
                            SuggestedMatch sm = new SuggestedMatch();
                            sm.setCv(cv);
                            sm.setJd(jd);
                            return sm;
                        });
                suggestedMatch.setSimilarityScore(BigDecimal.valueOf(eval.matchScore()));
                suggestedMatch.setMatchDate(Instant.now());
                suggestedMatchRepository.save(suggestedMatch);
            }
        }

        evaluations.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        return evaluations;
    }

    public List<CvVectorItem> searchCvsByKeyword(String keyword, int topK, String domainFilter) {
        String queryVector = embeddingHelper.embedToPgVectorString(keyword);
        List<Object[]> rows = cvRepository.findTopKCvsByVectorSimilarity(queryVector, topK, domainFilter);
        List<CvVectorItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            items.add(new CvVectorItem(
                    (UUID) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    ((Number) row[5]).doubleValue()
            ));
        }
        return items;
    }

    @Transactional
    public List<JdMatchEvaluationDto> matchJdsForCv(UUID cvId, int topK, String domainFilter) {
        log.info("Matching JDs cho CV: {}, TopK: {}", cvId, topK);

        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CV: " + cvId));

        String cvVector = cv.getVector();
        if (cvVector == null || cvVector.isBlank()) {
            cvVector = embeddingHelper.embedToPgVectorString(cv.getCvTitle() + " " + cv.getRawText());
            cv.setVector(cvVector);
            cvRepository.save(cv);
        }

        List<Object[]> rows = jdRepository.findTopKJdsByVectorSimilarity(cvVector, topK, domainFilter);
        List<JdMatchEvaluationDto> evaluations = new ArrayList<>();

        for (Object[] row : rows) {
            UUID jdId = (UUID) row[0];
            String jobTitle = (String) row[1];
            String companyName = (String) row[2];
            String domain = (String) row[3];
            String rawJdText = (String) row[4];
            double vectorSim = ((Number) row[5]).doubleValue();

            GeminiEvaluationService.EvaluationResult eval = geminiEvaluationService.evaluateMatch(
                    jobTitle,
                    rawJdText,
                    cv.getCvTitle(),
                    cv.getRawText()
            );

            JdMatchEvaluationDto dto = new JdMatchEvaluationDto(
                    jdId,
                    jobTitle,
                    companyName,
                    domain,
                    vectorSim,
                    eval.matchScore(),
                    eval.detailedReason(),
                    eval.skillGaps(),
                    eval.recommendations()
            );
            evaluations.add(dto);

            Jd jd = jdRepository.findById(jdId).orElse(null);
            if (jd != null) {
                SuggestedMatch suggestedMatch = suggestedMatchRepository.findByCvAndJd(cv, jd)
                        .orElseGet(() -> {
                            SuggestedMatch sm = new SuggestedMatch();
                            sm.setCv(cv);
                            sm.setJd(jd);
                            return sm;
                        });
                suggestedMatch.setSimilarityScore(BigDecimal.valueOf(eval.matchScore()));
                suggestedMatch.setMatchDate(Instant.now());
                suggestedMatchRepository.save(suggestedMatch);
            }
        }

        evaluations.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        return evaluations;
    }
}
