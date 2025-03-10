package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.kanji.KanjiCount;
import com.sangkeumi.mojimoji.dto.kanji.WrongKanji;
import com.sangkeumi.mojimoji.dto.mypage.*;
import com.sangkeumi.mojimoji.entity.*;

public interface KanjiCollectionsRepository extends JpaRepository<KanjiCollection, Long> {
    // userId와 kanjiId에 해당하는 첫 번째 획득 기록(생성일 기준)을 반환
    Optional<KanjiCollection> findFirstByUserUserIdAndKanji_KanjiIdOrderByCreatedAtAsc(Long userId, Long kanjiId);

    Optional<KanjiCollection> findByKanjiAndUser(Kanji kanji, User user);

    @Query(value = """
            SELECT
                '전체' AS jlptRank,
                COUNT(DISTINCT kc2.kanji_id) AS collected
            FROM Kanji_Collections kc2
            WHERE kc2.user_id = :userId

            UNION ALL

            SELECT
                k.jlpt_rank AS jlptRank,
                COUNT(DISTINCT kc.kanji_id) AS collected
            FROM Kanjis k
            LEFT JOIN Kanji_Collections kc
                ON k.kanji_id = kc.kanji_id
                AND kc.user_id = :userId
            GROUP BY k.jlpt_rank
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
            ORDER BY k.category, k.kanji_id;
            """, nativeQuery = true)
    List<CategoryKanjiRow> findCategoryKanjiRows(@Param("userId") Long userId);

    @Query(value = """
                SELECT
                    COUNT(k.kanji_id) AS totalCount,
                    SUM(CASE WHEN c.kanji_id IS NOT NULL THEN 1 ELSE 0 END) AS collectedCount
                FROM Kanjis k
                LEFT JOIN (
                    SELECT
                        kc.kanji_id
                        -- 여기선 MIN(...) 불필요, 그냥 수집 여부만 확인
                    FROM Kanji_Collections kc
                    WHERE kc.user_id = :userId
                    GROUP BY kc.kanji_id
                ) c ON k.kanji_id = c.kanji_id
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
            """, nativeQuery = true)
    KanjiCount findTotalAndCollected(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("jlptRank") String jlptRank,
            @Param("searchTerm") String searchTerm);

    @Query(value = """
            SELECT kc.kanji_collection_id,kc.user_id,kc.bookmarked,kc.collected_count,kc.wrong_count,kc.created_at,kc.updated_at,k.* FROM Kanji_Collections kc inner join kanjis k on kc.kanji_id = k.kanji_id  where kc.user_id = :userId and kc.wrong_count >=1 order by kc.wrong_count desc;
            """, nativeQuery = true)
    List<WrongKanji> findAllByUserId(@Param("userId") Long userId);

}
