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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sub_matches")
@Getter
@Setter
@NoArgsConstructor
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
}
