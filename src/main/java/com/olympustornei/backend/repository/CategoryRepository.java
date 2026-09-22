package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTournamentId(Long tournamentId);
}
