package com.recruitment.ai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suggested_matches", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cv_jd_suggest", columnNames = {"cv_id", "jd_id"})
})
public class SuggestedMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "match_id")
    private UUID matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private Cv cv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private Jd jd;

    @Column(name = "similarity_score", precision = 5, scale = 4)
    private BigDecimal similarityScore;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "match_date")
    private Instant matchDate = Instant.now();

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "gaps", columnDefinition = "TEXT")
    private String gaps;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    public SuggestedMatch() {}

    public UUID getMatchId() { return matchId; }
    public void setMatchId(UUID matchId) { this.matchId = matchId; }

    public Cv getCv() { return cv; }
    public void setCv(Cv cv) { this.cv = cv; }

    public Jd getJd() { return jd; }
    public void setJd(Jd jd) { this.jd = jd; }

    public BigDecimal getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(BigDecimal similarityScore) { this.similarityScore = similarityScore; }

    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }

    public Instant getMatchDate() { return matchDate; }
    public void setMatchDate(Instant matchDate) { this.matchDate = matchDate; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getGaps() { return gaps; }
    public void setGaps(String gaps) { this.gaps = gaps; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}
