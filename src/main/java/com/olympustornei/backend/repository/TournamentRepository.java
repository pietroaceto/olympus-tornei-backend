package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
