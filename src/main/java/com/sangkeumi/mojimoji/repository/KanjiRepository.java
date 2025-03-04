package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    @Query(value = """
                SELECT
                    k.kanji_id           AS kanjiId,
                    k.kanji              AS kanji,
                    k.jlpt_rank          AS jlptRank,
                    k.category           AS category,
                    k.kor_onyomi         AS korOnyomi,
                    k.kor_kunyomi        AS korKunyomi,
                    k.jpn_onyomi         AS jpnOnyomi,
                    k.jpn_kunyomi        AS jpnKunyomi,
                    k.meaning            AS meaning,
                    CASE WHEN s.user_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected,
                    COALESCE(s.collection_count, 0)   AS collectionCount,
                    s.first_collected_at AS firstCollectedAt,
                    s.last_collected_at  AS lastCollectedAt
                FROM Kanjis k
                LEFT JOIN Kanji_Collection_Summary s
                        ON s.kanji_id = k.kanji_id
                        AND s.user_id = :userId
                WHERE (:category IS NULL OR :category = '' OR k.category = :category)
                    AND (:jlptRank IS NULL OR :jlptRank = '' OR k.jlpt_rank = :jlptRank)
                    AND (
                        :searchTerm IS NULL OR :searchTerm = ''
                        OR k.kanji       LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                    )
                ORDER BY k.kanji_id ASC
                LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<myCollectionRequest> findKanjiOrderByKanjiIdAsc(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("searchTerm") String searchTerm,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
                SELECT
                    k.kanji_id           AS kanjiId,
                    k.kanji              AS kanji,
                    k.jlpt_rank          AS jlptRank,
                    k.category           AS category,
                    k.kor_onyomi         AS korOnyomi,
                    k.kor_kunyomi        AS korKunyomi,
                    k.jpn_onyomi         AS jpnOnyomi,
                    k.jpn_kunyomi        AS jpnKunyomi,
                    k.meaning            AS meaning,
                    CASE WHEN s.user_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected,
                    COALESCE(s.collection_count, 0)   AS collectionCount,
                    s.first_collected_at AS firstCollectedAt,
                    s.last_collected_at  AS lastCollectedAt
                FROM Kanjis k
                LEFT JOIN Kanji_Collection_Summary s
                        ON s.kanji_id = k.kanji_id
                        AND s.user_id = :userId
                WHERE (:category IS NULL OR :category = '' OR k.category = :category)
                    AND (:jlptRank IS NULL OR :jlptRank = '' OR k.jlpt_rank = :jlptRank)
                    AND (
                        :searchTerm IS NULL OR :searchTerm = ''
                        OR k.kanji       LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                    )
                ORDER BY k.kanji_id DESC
                LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<myCollectionRequest> findKanjiOrderByKanjiIdDesc(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("searchTerm") String searchTerm,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
                SELECT
                    k.kanji_id           AS kanjiId,
                    k.kanji              AS kanji,
                    k.jlpt_rank          AS jlptRank,
                    k.category           AS category,
                    k.kor_onyomi         AS korOnyomi,
                    k.kor_kunyomi        AS korKunyomi,
                    k.jpn_onyomi         AS jpnOnyomi,
                    k.jpn_kunyomi        AS jpnKunyomi,
                    k.meaning            AS meaning,
                    CASE WHEN s.user_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected,
                    COALESCE(s.collection_count, 0)   AS collectionCount,
                    s.first_collected_at AS firstCollectedAt,
                    s.last_collected_at  AS lastCollectedAt
                FROM Kanjis k
                LEFT JOIN Kanji_Collection_Summary s
                        ON s.kanji_id = k.kanji_id
                        AND s.user_id = :userId
                WHERE (:category IS NULL OR :category = '' OR k.category = :category)
                    AND (:jlptRank IS NULL OR :jlptRank = '' OR k.jlpt_rank = :jlptRank)
                    AND (
                        :searchTerm IS NULL OR :searchTerm = ''
                        OR k.kanji       LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                    )
                ORDER BY s.first_collected_at ASC NULLS LAST
                LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<myCollectionRequest> findKanjiOrderByCollectedAsc(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("searchTerm") String searchTerm,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
                SELECT
                    k.kanji_id           AS kanjiId,
                    k.kanji              AS kanji,
                    k.jlpt_rank          AS jlptRank,
                    k.category           AS category,
                    k.kor_onyomi         AS korOnyomi,
                    k.kor_kunyomi        AS korKunyomi,
                    k.jpn_onyomi         AS jpnOnyomi,
                    k.jpn_kunyomi        AS jpnKunyomi,
                    k.meaning            AS meaning,
                    CASE WHEN s.user_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected,
                    COALESCE(s.collection_count, 0)   AS collectionCount,
                    s.first_collected_at AS firstCollectedAt,
                    s.last_collected_at  AS lastCollectedAt
                FROM Kanjis k
                LEFT JOIN Kanji_Collection_Summary s
                        ON s.kanji_id = k.kanji_id
                        AND s.user_id = :userId
                WHERE (:category IS NULL OR :category = '' OR k.category = :category)
                    AND (:jlptRank IS NULL OR :jlptRank = '' OR k.jlpt_rank = :jlptRank)
                    AND (
                        :searchTerm IS NULL OR :searchTerm = ''
                        OR k.kanji       LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.kor_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_onyomi  LIKE CONCAT('%', :searchTerm, '%')
                        OR k.jpn_kunyomi LIKE CONCAT('%', :searchTerm, '%')
                    )
                ORDER BY s.first_collected_at DESC NULLS LAST
                LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<myCollectionRequest> findKanjiOrderByCollectedDesc(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("searchTerm") String searchTerm,
            @Param("limit") int limit,
            @Param("offset") int offset);

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
