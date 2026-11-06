package com.recruitment.ai.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ApplyJobDto {
    private UUID applyId;
    private UUID cvId;
    private String cvTitle;
    private String candidateName;
    private UUID jdId;
    private String jobTitle;
    private String companyName;
    private String status;
    private Instant applyTime;
    private BigDecimal matchScore;

    public ApplyJobDto() {}

    public UUID getApplyId() { return applyId; }
    public void setApplyId(UUID applyId) { this.applyId = applyId; }

    public UUID getCvId() { return cvId; }
    public void setCvId(UUID cvId) { this.cvId = cvId; }

    public String getCvTitle() { return cvTitle; }
    public void setCvTitle(String cvTitle) { this.cvTitle = cvTitle; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public UUID getJdId() { return jdId; }
    public void setJdId(UUID jdId) { this.jdId = jdId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getApplyTime() { return applyTime; }
    public void setApplyTime(Instant applyTime) { this.applyTime = applyTime; }

    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }
}
