package com.recruitment.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cvs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cv_id")
    private UUID cvId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cvList"})
    private Candidate candidate;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "cv_title", nullable = false)
    private String cvTitle;

    @Column(name = "domain")
    private String domain;

    @Column(name = "experience")
    private String experience;

    @Column(name = "khu_vuc")
    private String khuVuc;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "vector", columnDefinition = "vector(768)")
    private String vector;

    @Column(name = "meta_data", columnDefinition = "jsonb")
    private String metaData;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @JsonIgnore
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CvChunk> chunks = new ArrayList<>();

    public Cv() {}

    public UUID getCvId() { return cvId; }
    public void setCvId(UUID cvId) { this.cvId = cvId; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCvTitle() { return cvTitle; }
    public void setCvTitle(String cvTitle) { this.cvTitle = cvTitle; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getVector() { return vector; }
    public void setVector(String vector) { this.vector = vector; }

    public String getMetaData() { return metaData; }
    public void setMetaData(String metaData) { this.metaData = metaData; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<CvChunk> getChunks() { return chunks; }
    public void setChunks(List<CvChunk> chunks) { this.chunks = chunks; }
}
