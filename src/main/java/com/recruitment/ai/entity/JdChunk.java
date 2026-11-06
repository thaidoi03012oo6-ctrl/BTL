package com.recruitment.ai.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "jd_chunks")
public class JdChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chunk_id")
    private UUID chunkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private Jd jd;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "vector", columnDefinition = "vector(768)", nullable = false)
    private String vector;

    @Column(name = "meta_data", columnDefinition = "jsonb")
    private String metaData;

    public JdChunk() {}

    public UUID getChunkId() { return chunkId; }
    public void setChunkId(UUID chunkId) { this.chunkId = chunkId; }

    public Jd getJd() { return jd; }
    public void setJd(Jd jd) { this.jd = jd; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getVector() { return vector; }
    public void setVector(String vector) { this.vector = vector; }

    public String getMetaData() { return metaData; }
    public void setMetaData(String metaData) { this.metaData = metaData; }
}
