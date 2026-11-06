package com.recruitment.ai.repository;

import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.JdChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JdChunkRepository extends JpaRepository<JdChunk, UUID> {
    List<JdChunk> findByJd(Jd jd);
}
