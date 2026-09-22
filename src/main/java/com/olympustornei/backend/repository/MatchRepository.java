package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchPhase;
import com.olympustornei.backend.domain.MatchStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByCategoryIdAndPhase(Long categoryId, MatchPhase phase);

    boolean existsByCategoryIdAndPhase(Long categoryId, MatchPhase phase);

    boolean existsByCategoryIdAndStatus(Long categoryId, MatchStatus status);

    boolean existsByCategoryIdAndPhaseAndStatus(Long categoryId, MatchPhase phase, MatchStatus status);

    Optional<Match> findByCategoryIdAndPhaseAndBracketRoundIndexAndBracketSlot(
            Long categoryId, MatchPhase phase, Integer bracketRoundIndex, Integer bracketSlot);
}
