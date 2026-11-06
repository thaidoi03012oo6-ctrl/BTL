package com.recruitment.ai.dto.response;

import java.util.UUID;

public class CvSearchResultDto {
    private UUID cvId;
    private String cvTitle;
    private String candidateName;
    private String domain;
    private String experience;
    private String khuVuc;
    private String fileUrl;
    private String metaData;
    private double similarityScore;

    public CvSearchResultDto() {}

    public CvSearchResultDto(UUID cvId, String cvTitle, String candidateName, String domain,
                             String experience, String khuVuc, String fileUrl, String metaData, double similarityScore) {
        this.cvId = cvId;
        this.cvTitle = cvTitle;
        this.candidateName = candidateName;
        this.domain = domain;
        this.experience = experience;
        this.khuVuc = khuVuc;
        this.fileUrl = fileUrl;
        this.metaData = metaData;
        this.similarityScore = similarityScore;
    }

    public UUID getCvId() { return cvId; }
    public void setCvId(UUID cvId) { this.cvId = cvId; }

    public String getCvTitle() { return cvTitle; }
    public void setCvTitle(String cvTitle) { this.cvTitle = cvTitle; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getMetaData() { return metaData; }
    public void setMetaData(String metaData) { this.metaData = metaData; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
