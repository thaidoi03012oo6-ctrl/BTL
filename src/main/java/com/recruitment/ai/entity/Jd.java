package com.recruitment.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jds")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Jd {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "jd_id")
    private UUID jdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "jdList"})
    private Recruitment recruitment;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "status")
    private String status = "OPEN";

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "min_salary")
    private BigDecimal minSalary;

    @Column(name = "max_salary")
    private BigDecimal maxSalary;

    @Column(name = "experience")
    private String experience;

    @Column(name = "khu_vuc")
    private String khuVuc;

    @Column(name = "domain")
    private String domain;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "vector", columnDefinition = "vector(768)")
    private String vector;

    @Column(name = "meta_data", columnDefinition = "jsonb")
    private String metaData;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @JsonIgnore
    @OneToMany(mappedBy = "jd", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JdChunk> chunks = new ArrayList<>();

    public Jd() {}

    public UUID getJdId() { return jdId; }
    public void setJdId(UUID jdId) { this.jdId = jdId; }

    public Recruitment getRecruitment() { return recruitment; }
    public void setRecruitment(Recruitment recruitment) { this.recruitment = recruitment; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public BigDecimal getMinSalary() { return minSalary; }
    public void setMinSalary(BigDecimal minSalary) { this.minSalary = minSalary; }

    public BigDecimal getMaxSalary() { return maxSalary; }
    public void setMaxSalary(BigDecimal maxSalary) { this.maxSalary = maxSalary; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getVector() { return vector; }
    public void setVector(String vector) { this.vector = vector; }

    public String getMetaData() { return metaData; }
    public void setMetaData(String metaData) { this.metaData = metaData; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<JdChunk> getChunks() { return chunks; }
    public void setChunks(List<JdChunk> chunks) { this.chunks = chunks; }
}
