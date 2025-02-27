package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    List<Kanji> findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(String category, String jlptRank,
            String kanjiSearch);

    @Query(value = """
            SELECT
                k.kanji_id          AS kanjiId,
                k.kanji             AS kanji,
                k.jlpt_rank         AS jlptRank,
                k.category          AS category,
                k.kor_onyomi        AS korOnyomi,
                k.kor_kunyomi       AS korKunyomi,
                k.jpn_onyomi        AS jpnOnyomi,
                k.jpn_kunyomi       AS jpnKunyomi,
                k.meaning           AS meaning,
                CASE WHEN c.kanji_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected,
                c.first_collected_at AS firstCollectedAt
            FROM Kanjis k
            LEFT JOIN (
                SELECT
                    kc.kanji_id,
                    MIN(kc.created_at) AS first_collected_at
                FROM Kanji_Collections kc
                WHERE kc.user_id = :userId
                GROUP BY kc.kanji_id
            ) c ON k.kanji_id = c.kanji_id
            where k.category LIKE %:category% and k.jlpt_rank LIKE %:jlptRank% and (k.kanji LIKE %:kanjiSearch% or k.kor_kunyomi LIKE %:kanjiSearch% or k.kor_onyomi LIKE %:kanjiSearch% or k.jpn_kunyomi LIKE %:kanjiSearch% or k.jpn_onyomi LIKE %:kanjiSearch%)
            ORDER BY :Sort
            """, nativeQuery = true)

    List<myCollectionRequest> findKanjiCollectionStatusByUserId(@Param("userId") Long userId,
            @Param("category") String category, @Param("jlptRank") String jlptRank,
            @Param("kanjiSearch") String kanjiSearch, @Param("Sort") Integer Sort,
            @Param("sortDirection") String sortDirection);

    /**
     * 특정 책(Book)의 bookId를 사용하여 해당 책에서 사용된 한자 목록 조회
     */
    @Query("SELECT k FROM Kanji k " +
            "JOIN UsedBookKanji ubk ON ubk.kanji.kanjiId = k.kanjiId " +
            "JOIN ubk.bookLine bl " +
            "WHERE bl.book.bookId = :bookId")
    List<Kanji> findKanjisUsedInBook(@Param("bookId") Long bookId);

    Optional<Kanji> findByKanji(String kanji);
}
