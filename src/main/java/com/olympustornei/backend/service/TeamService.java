package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.Player;
import com.olympustornei.backend.domain.Team;
import com.olympustornei.backend.dto.PlayerRequest;
import com.olympustornei.backend.dto.PlayerResponse;
import com.olympustornei.backend.dto.TeamCreateRequest;
import com.olympustornei.backend.dto.TeamResponse;
import com.olympustornei.backend.dto.TeamUpdateRequest;
import com.olympustornei.backend.repository.PlayerRepository;
import com.olympustornei.backend.repository.TeamRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final CategoryService categoryService;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository,
                        CategoryService categoryService) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.categoryService = categoryService;
    }

    public TeamResponse create(Long categoryId, TeamCreateRequest request) {
        Category category = requireUnlockedCategory(categoryId);

        Team team = new Team();
        team.setCategory(category);
        team.setName(request.name());
        teamRepository.save(team);

        if (request.players() != null) {
            for (String playerName : request.players()) {
                Player player = new Player();
                player.setTeam(team);
                player.setName(playerName);
                playerRepository.save(player);
            }
        }
        return toResponse(team);
    }

    public TeamResponse update(Long id, TeamUpdateRequest request) {
        Team team = findEntity(id);
        requireUnlockedCategory(team.getCategory().getId());
        team.setName(request.name());
        return toResponse(team);
    }

    public void delete(Long id) {
        Team team = findEntity(id);
        requireUnlockedCategory(team.getCategory().getId());
        teamRepository.delete(team);
    }

    public PlayerResponse addPlayer(Long teamId, PlayerRequest request) {
        Team team = findEntity(teamId);
        requireUnlockedCategory(team.getCategory().getId());
        Player player = new Player();
        player.setTeam(team);
        player.setName(request.name());
        playerRepository.save(player);
        return toPlayerResponse(player);
    }

    public PlayerResponse updatePlayer(Long playerId, PlayerRequest request) {
        Player player = findPlayerEntity(playerId);
        requireUnlockedCategory(player.getTeam().getCategory().getId());
        player.setName(request.name());
        return toPlayerResponse(player);
    }

    public void deletePlayer(Long playerId) {
        Player player = findPlayerEntity(playerId);
        requireUnlockedCategory(player.getTeam().getCategory().getId());
        playerRepository.delete(player);
    }

    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listByCategory(Long categoryId) {
        return teamRepository.findByCategoryId(categoryId).stream().map(this::toResponse).toList();
    }

    private Category requireUnlockedCategory(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        if (category.isScheduleLocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Categoria bloccata: sono già stati inseriti risultati, la rosa non è più modificabile");
        }
        return category;
    }

    Team findEntity(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Player findPlayerEntity(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private TeamResponse toResponse(Team team) {
        List<PlayerResponse> players = playerRepository.findByTeamId(team.getId()).stream()
                .map(this::toPlayerResponse)
                .toList();
        return new TeamResponse(team.getId(), team.getCategory().getId(), team.getName(), players);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        return new PlayerResponse(player.getId(), player.getName());
    }
}
