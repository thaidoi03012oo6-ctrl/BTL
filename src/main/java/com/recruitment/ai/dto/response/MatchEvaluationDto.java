package com.recruitment.ai.dto.response;

import java.util.List;
import java.util.UUID;

public class MatchEvaluationDto {
    private UUID cvId;
    private String candidateName;
    private String cvTitle;
    private String domain;
    private double vectorSimilarity;
    private int matchScore;
    private String detailedReason;
    private List<String> skillGaps;
    private List<String> recommendations;

    public MatchEvaluationDto() {}

    public MatchEvaluationDto(UUID cvId, String candidateName, String cvTitle, String domain, double vectorSimilarity, int matchScore, String detailedReason, List<String> skillGaps, List<String> recommendations) {
        this.cvId = cvId;
        this.candidateName = candidateName;
        this.cvTitle = cvTitle;
        this.domain = domain;
        this.vectorSimilarity = vectorSimilarity;
        this.matchScore = matchScore;
        this.detailedReason = detailedReason;
        this.skillGaps = skillGaps;
        this.recommendations = recommendations;
    }

    public UUID getCvId() { return cvId; }
    public void setCvId(UUID cvId) { this.cvId = cvId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCvTitle() { return cvTitle; }
    public void setCvTitle(String cvTitle) { this.cvTitle = cvTitle; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public double getVectorSimilarity() { return vectorSimilarity; }
    public void setVectorSimilarity(double vectorSimilarity) { this.vectorSimilarity = vectorSimilarity; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public String getDetailedReason() { return detailedReason; }
    public void setDetailedReason(String detailedReason) { this.detailedReason = detailedReason; }

    public List<String> getSkillGaps() { return skillGaps; }
    public void setSkillGaps(List<String> skillGaps) { this.skillGaps = skillGaps; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
