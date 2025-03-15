package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sangkeumi.mojimoji.dto.kanji.*;
import com.sangkeumi.mojimoji.dto.mypage.*;
import com.sangkeumi.mojimoji.entity.*;

public interface KanjiCollectionsRepository extends JpaRepository<KanjiCollection, Long> {
    /** userId와 kanjiId에 해당하는 획득 기록을 반환 */
    Optional<KanjiCollection> findByUserUserIdAndKanji_KanjiId(Long userId, Long kanjiId);
    Optional<KanjiCollection> findByKanjiAndUser(Kanji kanji, User user);

    @Query(value = """
            SELECT
                '전체' AS jlptRank,
                COUNT(DISTINCT kc2.kanji_id) AS collected
            FROM Kanji_Collections kc2
            WHERE kc2.user_id = :userId
              AND kc2.collected_count <> 0

            UNION ALL

            SELECT
                k.jlpt_rank AS jlptRank,
                COUNT(DISTINCT kc.kanji_id) AS collected
            FROM Kanjis k
            LEFT JOIN Kanji_Collections kc
                ON k.kanji_id = kc.kanji_id
                AND kc.user_id = :userId
                AND kc.collected_count <> 0
            GROUP BY k.jlpt_rank;

                """, nativeQuery = true)
    // ↑ 멀티라인 문자열(JDK 15+) 또는 기존 문자열 결합 사용 가능
    // (문자열 안에 세미콜론은 넣지 않는 것이 안전)
    List<JlptCollectionStats> findJlptStatsByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT
                CAST(created_at AS DATE) AS acquisition_date,
                COUNT(*) AS daily_count
            FROM Kanji_Collections
            WHERE user_id = :userId
                AND collected_count > 0
            GROUP BY CAST(created_at AS DATE)
            ORDER BY CAST(created_at AS DATE)
            """, nativeQuery = true)
    List<DailyAcquisitionStats> findDailyStatsByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT ROUND(AVG(sub.daily_count), 2)
            FROM (
                SELECT
                    CAST(created_at AS DATE) AS acquisition_date,
                    COUNT(*) AS daily_count
                FROM Kanji_Collections
                WHERE user_id = :userId
                  AND collected_count <> 0
                GROUP BY CAST(created_at AS DATE)
            ) AS sub
            """, nativeQuery = true)
    Double findDailyAverageByUserId(@Param("userId") Long userId);

    @Query(value = """
            WITH cat_total AS (
                SELECT category, COUNT(*) AS total_kanjis
                FROM Kanjis
                GROUP BY category
            ),
            cat_collected AS (
                SELECT k.category, COUNT(DISTINCT kc.kanji_id) AS collected_kanjis
                FROM Kanjis k
                LEFT JOIN Kanji_Collections kc
                    ON k.kanji_id = kc.kanji_id
                    AND kc.user_id = :userId
                    AND kc.collected_count <> 0
                GROUP BY k.category
            ),
            fully_collected AS (
                SELECT t.category
                FROM cat_total t
                JOIN cat_collected c
                    ON t.category = c.category
                WHERE t.total_kanjis = c.collected_kanjis
                )
                SELECT
                    total_part.total_category_count,
                    fully_part.fully_collected_category_count
                FROM (
                    SELECT COUNT(*) AS total_category_count
                    FROM cat_total
                ) AS total_part
                CROSS JOIN (
                    SELECT COUNT(*) AS fully_collected_category_count
                    FROM fully_collected
                ) AS fully_part
            """, nativeQuery = true)
    CategoryCollectionSummary findCategoryCollectionSummary(@Param("userId") Long userId);

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
                COALESCE(kc.createdAt, '9999-12-31 23:59:59') AS firstCollectedAt
            )
            FROM Kanji k
            LEFT JOIN KanjiCollection kc
                ON k.kanjiId = kc.kanji.kanjiId
                AND kc.user.id = :userId
                AND kc.collectedCount > 0
            WHERE (:category IS NULL OR k.category = :category)
                AND (:jlptRank IS NULL OR k.jlptRank = :jlptRank)
                AND (
                    :kanjiSearch IS NULL OR
                    k.kanji       LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.korKunyomi  LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.korOnyomi   LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.jpnKunyomi  LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.jpnOnyomi   LIKE CONCAT('%', :kanjiSearch, '%')
                )
            """)
    Page<KanjiSearchResponse> findMyCollection(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("kanjiSearch") String kanjiSearch,
            Pageable pageable);

    @Query("""
            SELECT new com.sangkeumi.mojimoji.dto.kanji.KanjiCount(
                COUNT(k),
                COALESCE(SUM(CASE WHEN kc IS NOT NULL THEN 1 ELSE 0 END), 0)
            )
            FROM Kanji k
            LEFT JOIN KanjiCollection kc
                ON k.kanjiId = kc.kanji.kanjiId
                AND kc.user.id = :userId
                AND kc.collectedCount > 0
            WHERE (:category IS NULL OR k.category = :category)
                AND (:jlptRank IS NULL OR k.jlptRank = :jlptRank)
                AND (
                    :kanjiSearch IS NULL OR
                    k.kanji       LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.korKunyomi  LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.korOnyomi   LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.jpnKunyomi  LIKE CONCAT('%', :kanjiSearch, '%') OR
                    k.jpnOnyomi   LIKE CONCAT('%', :kanjiSearch, '%')
                    )
            """)
    KanjiCount findTotalAndCollected(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("kanjiSearch") String kanjiSearch);

    @Query(value = """
            SELECT DISTINCT
                k.category   AS category,
                k.jlpt_rank  AS jlptRank,
                k.kanji_id   AS kanjiId,
                k.kanji      AS kanji,
                CASE WHEN kc.kanji_id IS NOT NULL THEN 1 ELSE 0 END AS isCollected
            FROM Kanjis k
            LEFT JOIN Kanji_Collections kc
                ON k.kanji_id = kc.kanji_id
                AND kc.user_id = :userId
                AND kc.collected_count <> 0
            ORDER BY k.category, k.kanji_id;
            """, nativeQuery = true)
    List<CategoryKanjiRow> findCategoryKanjiRows(@Param("userId") Long userId);

    /**
     * 북마크된 경우에 해당하는 한자 목록 조회
     */
    @Query("""
            SELECT new com.sangkeumi.mojimoji.dto.kanji.BookmarkedKanjiDTO(
                k.kanjiId, k.kanji, k.korOnyomi, k.korKunyomi, k.jpnOnyomi, k.jpnKunyomi, k.meaning)
            FROM KanjiCollection kc
            JOIN kc.kanji k
            WHERE kc.user.userId = :userId AND kc.bookmarked = 1
            """)
    List<BookmarkedKanjiDTO> findBookmarkedKanjis(@Param("userId") Long userId);

    @Query(value = """
                SELECT kc.kanji_collection_id,kc.user_id,kc.bookmarked,kc.collected_count,kc.wrong_count,kc.created_at,kc.updated_at,k.* FROM Kanji_Collections kc inner join kanjis k on kc.kanji_id = k.kanji_id  where kc.user_id = :userId and kc.wrong_count >=1 order by kc.updated_at desc;
            """, nativeQuery = true)
    List<WrongKanji> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT new com.sangkeumi.mojimoji.dto.kanji.QuizKanjiDTO(
                k.kanjiId, k.kanji, k.jlptRank, k.category, k.korOnyomi, k.korKunyomi, k.jpnOnyomi, k.jpnKunyomi, k.meaning, kc.bookmarked)
            FROM Kanji k
            JOIN UsedBookKanji ubk ON ubk.kanji.kanjiId = k.kanjiId
            JOIN ubk.bookLine bl
            LEFT JOIN KanjiCollection kc ON kc.kanji.kanjiId = k.kanjiId AND kc.user.userId = :userId
            WHERE bl.book.bookId = :bookId
            """)
    List<QuizKanjiDTO> findKanjisToQuiz(@Param("bookId") Long bookId, @Param("userId") Long userId);
}
