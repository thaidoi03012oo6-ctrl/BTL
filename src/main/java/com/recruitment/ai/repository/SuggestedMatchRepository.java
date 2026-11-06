package com.recruitment.ai.repository;

import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.SuggestedMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuggestedMatchRepository extends JpaRepository<SuggestedMatch, UUID> {
    Optional<SuggestedMatch> findByCvAndJd(Cv cv, Jd jd);
    List<SuggestedMatch> findByJdOrderByAiScoreDesc(Jd jd);
    List<SuggestedMatch> findByCvOrderByAiScoreDesc(Cv cv);
    void deleteByCv(Cv cv);
    void deleteByJd(Jd jd);
}
