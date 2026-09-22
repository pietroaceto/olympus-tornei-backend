package com.olympustornei.backend.repository;

import com.olympustornei.backend.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCategoryId(Long categoryId);
}
