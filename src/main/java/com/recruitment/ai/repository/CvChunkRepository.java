package com.recruitment.ai.repository;

import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.CvChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CvChunkRepository extends JpaRepository<CvChunk, UUID> {

    List<CvChunk> findByCv(Cv cv);

    @Query(value = """
        SELECT 
            c.cv_id AS parent_id,
            c.cv_title AS title,
            cc.content AS chunk_content,
            (1 - (cc.vector <=> CAST(:queryVector AS vector))) AS similarity
        FROM cv_chunks cc
        JOIN cvs c ON cc.cv_id = c.cv_id
        ORDER BY cc.vector <=> CAST(:queryVector AS vector) ASC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> findTopRelevantCvChunks(
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );
}
