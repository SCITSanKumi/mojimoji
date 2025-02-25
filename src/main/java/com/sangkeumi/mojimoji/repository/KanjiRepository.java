package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    List<Kanji> findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(String category, String jlptRank,
            String kanjiSearch);

}
