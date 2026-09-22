package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.CategoryPhase;
import com.olympustornei.backend.domain.CompetitionFormat;
import com.olympustornei.backend.domain.MatchFormat;
import com.olympustornei.backend.domain.Tournament;
import com.olympustornei.backend.dto.CategoryRequest;
import com.olympustornei.backend.dto.CategoryResponse;
import com.olympustornei.backend.repository.CategoryRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TournamentService tournamentService;

    public CategoryService(CategoryRepository categoryRepository, TournamentService tournamentService) {
        this.categoryRepository = categoryRepository;
        this.tournamentService = tournamentService;
    }

    public CategoryResponse create(Long tournamentId, CategoryRequest request) {
        Tournament tournament = tournamentService.findEntity(tournamentId);

        Category category = new Category();
        category.setTournament(tournament);
        category.setName(request.name());
        category.setMatchFormat(request.matchFormat());
        category.setSubMatchesCount(resolveSubMatchesCount(request));
        category.setCompetitionFormat(resolveCompetitionFormat(request));
        category.setPhase(CategoryPhase.GIRONE);
        category.setScheduleLocked(false);
        categoryRepository.save(category);
        return toResponse(category);
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        if (category.isScheduleLocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Categoria bloccata: sono già stati inseriti risultati");
        }
        category.setName(request.name());
        category.setMatchFormat(request.matchFormat());
        category.setSubMatchesCount(resolveSubMatchesCount(request));
        category.setCompetitionFormat(resolveCompetitionFormat(request));
        return toResponse(category);
    }

    public void delete(Long id) {
        Category category = findEntity(id);
        if (category.isScheduleLocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Categoria bloccata: sono già stati inseriti risultati");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listByTournament(Long tournamentId) {
        return categoryRepository.findByTournamentId(tournamentId).stream().map(this::toResponse).toList();
    }

    Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private int resolveSubMatchesCount(CategoryRequest request) {
        if (request.matchFormat() == MatchFormat.SINGLE) {
            return 1;
        }
        return request.subMatchesCount() != null && request.subMatchesCount() >= 1 ? request.subMatchesCount() : 1;
    }

    private CompetitionFormat resolveCompetitionFormat(CategoryRequest request) {
        return request.competitionFormat() != null ? request.competitionFormat() : CompetitionFormat.GIRONE;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getTournament().getId(),
                category.getName().name(),
                category.getMatchFormat().name(),
                category.getSubMatchesCount(),
                category.getPhase().name(),
                category.isScheduleLocked(),
                category.getCompetitionFormat().name());
    }
}
