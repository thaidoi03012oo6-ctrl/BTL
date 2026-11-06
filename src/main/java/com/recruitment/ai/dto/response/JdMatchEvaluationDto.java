package com.recruitment.ai.dto.response;

import java.util.List;
import java.util.UUID;

public class JdMatchEvaluationDto {
    private UUID jdId;
    private String jobTitle;
    private String companyName;
    private String domain;
    private double vectorSimilarity;
    private int matchScore;
    private String detailedReason;
    private List<String> skillGaps;
    private List<String> recommendations;

    public JdMatchEvaluationDto() {}

    public JdMatchEvaluationDto(UUID jdId, String jobTitle, String companyName, String domain, double vectorSimilarity, int matchScore, String detailedReason, List<String> skillGaps, List<String> recommendations) {
        this.jdId = jdId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.domain = domain;
        this.vectorSimilarity = vectorSimilarity;
        this.matchScore = matchScore;
        this.detailedReason = detailedReason;
        this.skillGaps = skillGaps;
        this.recommendations = recommendations;
    }

    public UUID getJdId() { return jdId; }
    public void setJdId(UUID jdId) { this.jdId = jdId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

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
