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
    /**
     * Primary Key, 자동 생성
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kanji_id")
    private Long kanjiId;

    /**
     * JLPT 등급
     */
    @Column(name = "jlpt_rank")
    private String jlptRank;

    /**
     * 한자 분류
     */
    private String category;

    /**
     * 한자 (최대 4자)
     */
    @Column(nullable = false, length = 4)
    private String kanji;

    /**
     * 한자의 한국식 온음
     */
    @Column(name = "kor_onyomi")
    private String korOnyomi;

    /**
     * 한자의 한국식 음독
     */
    @Column(name = "kor_kunyomi")
    private String korKunyomi;

    /**
     * 한자의 일본식 온음
     */
    @Column(name = "jpn_onyomi")
    private String jpnOnyomi;

    /**
     * 한자의 일본식 음독
     */
    @Column(name = "jpn_kunyomi")
    private String jpnKunyomi;

    /**
     * 한자 의미
     */
    @Column(length = 2000)
    private String meaning;

    // 양방향 관계

    /**
     * 이 한자가 사용된 책의 줄(UsedBookKanji)의 목록
     */
    @OneToMany(mappedBy = "kanji", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedBookKanji> usedBookKanjis;

    /**
     * 한자 컬렉션 (유저가 소장한 한자) 목록
     */
    @OneToMany(mappedBy = "kanji", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanjiCollection> kanjiCollections;
}
