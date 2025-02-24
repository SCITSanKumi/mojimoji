package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.dto.mypage.JlptCollectionStats;
import com.sangkeumi.mojimoji.entity.KanjiCollection;

public interface KanjiCollectionsRepository extends JpaRepository<KanjiCollection, Long> {

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
}
