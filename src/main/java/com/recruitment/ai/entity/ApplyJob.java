package com.recruitment.ai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "apply_jobs", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cv_jd_apply", columnNames = {"cv_id", "jd_id"})
})
public class ApplyJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "apply_id")
    private UUID applyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private Cv cv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private Jd jd;

    @Column(name = "apply_time")
    private Instant applyTime = Instant.now();

    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "status")
    private String status = "PENDING";

    public ApplyJob() {}

    public UUID getApplyId() { return applyId; }
    public void setApplyId(UUID applyId) { this.applyId = applyId; }

    public Cv getCv() { return cv; }
    public void setCv(Cv cv) { this.cv = cv; }

    public Jd getJd() { return jd; }
    public void setJd(Jd jd) { this.jd = jd; }

    public Instant getApplyTime() { return applyTime; }
    public void setApplyTime(Instant applyTime) { this.applyTime = applyTime; }

    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
