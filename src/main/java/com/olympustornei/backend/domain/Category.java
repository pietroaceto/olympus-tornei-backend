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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
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
}
