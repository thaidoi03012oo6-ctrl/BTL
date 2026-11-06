package com.recruitment.ai.repository;

import com.recruitment.ai.entity.Candidate;
import com.recruitment.ai.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CvRepository extends JpaRepository<Cv, UUID> {

    List<Cv> findByCandidate(Candidate candidate);
    List<Cv> findByDomain(String domain);

    @Query(value = """
        SELECT 
            c.cv_id,
            c.cv_title,
            cand.full_name,
            c.domain,
            c.raw_text,
            (1 - (c.vector <=> CAST(:jdVector AS vector))) AS similarity
        FROM cvs c
        JOIN candidates cand ON c.candidate_id = cand.user_id
        WHERE c.vector IS NOT NULL
          AND (:domainFilter IS NULL OR c.domain = :domainFilter)
        ORDER BY c.vector <=> CAST(:jdVector AS vector) ASC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> findTopKCvsByVectorSimilarity(
            @Param("jdVector") String jdVector,
            @Param("topK") int topK,
            @Param("domainFilter") String domainFilter
    );

    @Query(value = """
        SELECT 
            c.cv_id,
            c.cv_title,
            cand.full_name,
            c.domain,
            c.experience,
            c.khu_vuc,
            c.file_url,
            c.meta_data,
            (1 - (c.vector <=> CAST(:vector AS vector))) AS similarity
        FROM cvs c
        JOIN candidates cand ON c.candidate_id = cand.user_id
        WHERE c.vector IS NOT NULL
          AND (:domain IS NULL OR :domain = '' OR LOWER(c.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
          AND (:khuVuc IS NULL OR :khuVuc = '' OR LOWER(c.khu_vuc) LIKE LOWER(CONCAT('%', :khuVuc, '%')))
          AND (
               :keyword IS NULL OR :keyword = ''
               OR LOWER(c.cv_title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.raw_text) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(cand.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY c.vector <=> CAST(:vector AS vector) ASC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> searchCvs(
            @Param("vector") String vector,
            @Param("keyword") String keyword,
            @Param("domain") String domain,
            @Param("khuVuc") String khuVuc,
            @Param("topK") int topK
    );
}
