package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ApplyJob;
import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.Jd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplyJobRepository extends JpaRepository<ApplyJob, UUID> {
    Optional<ApplyJob> findByCvAndJd(Cv cv, Jd jd);

    @Query("SELECT a FROM ApplyJob a JOIN FETCH a.cv c JOIN FETCH c.candidate WHERE a.jd.recruitment.userId = :recruiterId ORDER BY a.applyTime DESC")
    List<ApplyJob> findByRecruiterId(@Param("recruiterId") UUID recruiterId);

    @Query("SELECT a FROM ApplyJob a JOIN FETCH a.jd j JOIN FETCH j.recruitment WHERE a.cv.candidate.userId = :candidateId ORDER BY a.applyTime DESC")
    List<ApplyJob> findByCandidateId(@Param("candidateId") UUID candidateId);

    @Query("SELECT a FROM ApplyJob a JOIN FETCH a.jd j JOIN FETCH j.recruitment WHERE a.cv.candidate.userId = :candidateId AND a.jd.jdId = :jdId")
    Optional<ApplyJob> findByCandidateIdAndJdId(@Param("candidateId") UUID candidateId, @Param("jdId") UUID jdId);

    void deleteByCv(Cv cv);
    void deleteByJd(Jd jd);
}
