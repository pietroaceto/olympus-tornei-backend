package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.MatchdayRound;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchdayRoundRepository extends JpaRepository<MatchdayRound, Long> {

    List<MatchdayRound> findByCategoryIdOrderByRoundNumberAsc(Long categoryId);
}
