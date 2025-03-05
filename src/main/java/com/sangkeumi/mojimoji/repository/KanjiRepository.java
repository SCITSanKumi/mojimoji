package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.kanji.KanjiSearchResponse;
import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    Optional<Kanji> findByKanji(String kanji);

    /**
     * 특정 책(Book)의 bookId를 사용하여 해당 책에서 사용된 한자 목록 조회
     */
    @Query("""
            SELECT k FROM Kanji k
                JOIN UsedBookKanji ubk ON ubk.kanji.kanjiId = k.kanjiId
                JOIN ubk.bookLine bl
                WHERE bl.book.bookId = :bookId
            """)
    List<Kanji> findKanjisUsedInBook(@Param("bookId") Long bookId);

    @Query("""
                SELECT new com.sangkeumi.mojimoji.dto.kanji.KanjiSearchResponse(
                    k.kanjiId                                 AS kanjiId,
                    k.kanji                                   AS kanji,
                    k.jlptRank                                AS jlptRank,
                    k.category                                AS category,
                    k.korOnyomi                               AS korOnyomi,
                    k.korKunyomi                              AS korKunyomi,
                    k.jpnOnyomi                               AS jpnOnyomi,
                    k.jpnKunyomi                              AS jpnKunyomi,
                    k.meaning                                 AS meaning,
                    COALESCE(kc.bookmarked, false)            AS bookmarked,
                    COALESCE(kc.collectedCount, 0)            AS collectedCount,
                    COALESCE(kc.createdAt, CURRENT_TIMESTAMP) AS firstCollectedAt
                )
                FROM Kanji k
                LEFT JOIN KanjiCollection kc
                    ON k.kanjiId = kc.kanji.kanjiId
                    AND kc.user.id = :userId
                WHERE k.category LIKE CONCAT('%', :category, '%')
                    AND k.jlptRank LIKE CONCAT('%', :jlptRank, '%')
                    AND (k.kanji LIKE CONCAT('%', :kanjiSearch, '%')
                        OR k.korKunyomi LIKE CONCAT('%', :kanjiSearch, '%')
                        OR k.korOnyomi LIKE CONCAT('%', :kanjiSearch, '%')
                        OR k.jpnKunyomi LIKE CONCAT('%', :kanjiSearch, '%')
                        OR k.jpnOnyomi LIKE CONCAT('%', :kanjiSearch, '%'))
            """)
    Page<KanjiSearchResponse> findMyCollection(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("kanjiSearch") String kanjiSearch,
            Pageable pageable);
}
