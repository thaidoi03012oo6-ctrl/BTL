package com.recruitment.ai.repository;

import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JdRepository extends JpaRepository<Jd, UUID> {

    List<Jd> findByRecruitment(Recruitment recruitment);
    List<Jd> findByStatus(String status);

    @Query(value = """
        SELECT 
            j.jd_id,
            j.job_title,
            r.company_name,
            j.domain,
            j.raw_text,
            (1 - (j.vector <=> CAST(:cvVector AS vector))) AS similarity
        FROM jds j
        JOIN recruitments r ON j.recruitment_id = r.user_id
        WHERE j.vector IS NOT NULL
          AND (:domainFilter IS NULL OR j.domain = :domainFilter)
        ORDER BY j.vector <=> CAST(:cvVector AS vector) ASC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> findTopKJdsByVectorSimilarity(
            @Param("cvVector") String cvVector,
            @Param("topK") int topK,
            @Param("domainFilter") String domainFilter
    );

    @Query(value = """
        SELECT 
            j.jd_id,
            j.job_title,
            r.company_name,
            j.domain,
            j.experience,
            j.khu_vuc,
            j.min_salary,
            j.max_salary,
            j.description,
            (1 - (j.vector <=> CAST(:vector AS vector))) AS similarity
        FROM jds j
        JOIN recruitments r ON j.recruitment_id = r.user_id
        WHERE j.vector IS NOT NULL
          AND (:domain IS NULL OR :domain = '' OR LOWER(j.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
          AND (:khuVuc IS NULL OR :khuVuc = '' OR LOWER(j.khu_vuc) LIKE LOWER(CONCAT('%', :khuVuc, '%')))
          AND (
               :keyword IS NULL OR :keyword = ''
               OR LOWER(j.job_title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.company_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY j.vector <=> CAST(:vector AS vector) ASC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> searchJds(
            @Param("vector") String vector,
            @Param("keyword") String keyword,
            @Param("domain") String domain,
            @Param("khuVuc") String khuVuc,
            @Param("topK") int topK
    );
}
