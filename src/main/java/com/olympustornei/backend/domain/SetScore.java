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
@Table(name = "set_scores")
@Getter
@Setter
@NoArgsConstructor
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
}
