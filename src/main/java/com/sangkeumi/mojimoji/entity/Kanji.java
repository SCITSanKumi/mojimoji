package com.sangkeumi.mojimoji.entity;

import java.util.List;

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
    @Column(name = "kanji_id")
    private Long kanjiId;

    @Column(name = "jlpt_rank", length = 10)
    private String jlptRank;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "kanji", nullable = false, length = 4)
    private String kanji;

    @Column(name = "kor_onyomi", length = 20)
    private String korOnyomi;

    @Column(name = "kor_kunyomi", length = 20)
    private String korKunyomi;

    @Column(name = "jpn_onyomi", length = 20)
    private String jpnOnyomi;

    @Column(name = "jpn_kunyomi", length = 20)
    private String jpnKunyomi;

    @Column(name = "meaning", length = 2000)
    private String meaning;

    @OneToMany(mappedBy = "kanji", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanjiCollection> kanjiCollections;

    @OneToMany(mappedBy = "kanji", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedKanji> usedKanjis;
}
