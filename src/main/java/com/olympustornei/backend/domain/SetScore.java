package com.olympustornei.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "set_scores")
public class SetScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_match_id", nullable = false)
    private SubMatch subMatch;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "home_games", nullable = false)
    private Integer homeGames;

    @Column(name = "away_games", nullable = false)
    private Integer awayGames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubMatch getSubMatch() {
        return subMatch;
    }

    public void setSubMatch(SubMatch subMatch) {
        this.subMatch = subMatch;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Integer getHomeGames() {
        return homeGames;
    }

    public void setHomeGames(Integer homeGames) {
        this.homeGames = homeGames;
    }

    public Integer getAwayGames() {
        return awayGames;
    }

    public void setAwayGames(Integer awayGames) {
        this.awayGames = awayGames;
    }
}
