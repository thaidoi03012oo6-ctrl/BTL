package com.recruitment.ai.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class JdSearchResultDto {
    private UUID jdId;
    private String jobTitle;
    private String companyName;
    private String domain;
    private String experience;
    private String khuVuc;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String description;
    private double similarityScore;

    public JdSearchResultDto() {}

    public JdSearchResultDto(UUID jdId, String jobTitle, String companyName, String domain,
                             String experience, String khuVuc, BigDecimal minSalary, BigDecimal maxSalary,
                             String description, double similarityScore) {
        this.jdId = jdId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.domain = domain;
        this.experience = experience;
        this.khuVuc = khuVuc;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.description = description;
        this.similarityScore = similarityScore;
    }

    public UUID getJdId() { return jdId; }
    public void setJdId(UUID jdId) { this.jdId = jdId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public BigDecimal getMinSalary() { return minSalary; }
    public void setMinSalary(BigDecimal minSalary) { this.minSalary = minSalary; }

    public BigDecimal getMaxSalary() { return maxSalary; }
    public void setMaxSalary(BigDecimal maxSalary) { this.maxSalary = maxSalary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
