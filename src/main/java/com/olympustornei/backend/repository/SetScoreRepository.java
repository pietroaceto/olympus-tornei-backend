package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.SetScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetScoreRepository extends JpaRepository<SetScore, Long> {

    List<SetScore> findBySubMatchIdOrderBySetNumberAsc(Long subMatchId);
}
