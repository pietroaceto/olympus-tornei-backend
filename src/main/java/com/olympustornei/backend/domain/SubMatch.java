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
@Table(name = "sub_matches")
public class SubMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private Integer ordine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_player1_id")
    private Player homePlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_player2_id")
    private Player homePlayer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_player1_id")
    private Player awayPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_player2_id")
    private Player awayPlayer2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Integer getOrdine() {
        return ordine;
    }

    public void setOrdine(Integer ordine) {
        this.ordine = ordine;
    }

    public Player getHomePlayer1() {
        return homePlayer1;
    }

    public void setHomePlayer1(Player homePlayer1) {
        this.homePlayer1 = homePlayer1;
    }

    public Player getHomePlayer2() {
        return homePlayer2;
    }

    public void setHomePlayer2(Player homePlayer2) {
        this.homePlayer2 = homePlayer2;
    }

    public Player getAwayPlayer1() {
        return awayPlayer1;
    }

    public void setAwayPlayer1(Player awayPlayer1) {
        this.awayPlayer1 = awayPlayer1;
    }

    public Player getAwayPlayer2() {
        return awayPlayer2;
    }

    public void setAwayPlayer2(Player awayPlayer2) {
        this.awayPlayer2 = awayPlayer2;
    }
}
