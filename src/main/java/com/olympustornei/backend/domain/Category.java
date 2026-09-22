package com.olympustornei.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryName name;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_format", nullable = false, length = 20)
    private MatchFormat matchFormat;

    @Column(name = "sub_matches_count", nullable = false)
    private Integer subMatchesCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryPhase phase = CategoryPhase.GIRONE;

    @Column(name = "schedule_locked", nullable = false)
    private boolean scheduleLocked = false;

    @Column(name = "bracket_total_rounds")
    private Integer bracketTotalRounds;

    @Enumerated(EnumType.STRING)
    @Column(name = "competition_format", nullable = false, length = 20)
    private CompetitionFormat competitionFormat = CompetitionFormat.GIRONE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public CategoryName getName() {
        return name;
    }

    public void setName(CategoryName name) {
        this.name = name;
    }

    public MatchFormat getMatchFormat() {
        return matchFormat;
    }

    public void setMatchFormat(MatchFormat matchFormat) {
        this.matchFormat = matchFormat;
    }

    public Integer getSubMatchesCount() {
        return subMatchesCount;
    }

    public void setSubMatchesCount(Integer subMatchesCount) {
        this.subMatchesCount = subMatchesCount;
    }

    public CategoryPhase getPhase() {
        return phase;
    }

    public void setPhase(CategoryPhase phase) {
        this.phase = phase;
    }

    public boolean isScheduleLocked() {
        return scheduleLocked;
    }

    public void setScheduleLocked(boolean scheduleLocked) {
        this.scheduleLocked = scheduleLocked;
    }

    public Integer getBracketTotalRounds() {
        return bracketTotalRounds;
    }

    public void setBracketTotalRounds(Integer bracketTotalRounds) {
        this.bracketTotalRounds = bracketTotalRounds;
    }

    public CompetitionFormat getCompetitionFormat() {
        return competitionFormat;
    }

    public void setCompetitionFormat(CompetitionFormat competitionFormat) {
        this.competitionFormat = competitionFormat;
    }
}
