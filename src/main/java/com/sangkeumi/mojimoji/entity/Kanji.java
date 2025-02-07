package com.sangkeumi.mojimoji.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Kanjis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kanji {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kanjiId;

    @Column(nullable = false, length = 4)
    private String kanji;

    private String kunon;
    private String onyomi;
    private String kunyomi;
    private String meaning;
    private String bushu;
    private String shapeComponent;

    @Column(nullable = false)
    private int strokeCount;

    private String jlptRank;
}
