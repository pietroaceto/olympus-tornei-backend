package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.SubMatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubMatchRepository extends JpaRepository<SubMatch, Long> {

    List<SubMatch> findByMatchIdOrderByOrdineAsc(Long matchId);
}
