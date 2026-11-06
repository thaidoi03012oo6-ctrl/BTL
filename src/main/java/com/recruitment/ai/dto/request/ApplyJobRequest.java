package com.recruitment.ai.dto.request;

import java.util.UUID;

public class ApplyJobRequest {
    private UUID cvId;
    private UUID jdId;

    public UUID getCvId() { return cvId; }
    public void setCvId(UUID cvId) { this.cvId = cvId; }

    public UUID getJdId() { return jdId; }
    public void setJdId(UUID jdId) { this.jdId = jdId; }
}
