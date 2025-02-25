package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.User;

public interface KanjiCollectionRepository extends JpaRepository<KanjiCollection, Long> {

        List<KanjiCollection> findAllByUser(User user);

        Optional<KanjiCollection> findByUserAndKanji(User user, Kanji kanji);

        @Query(value = """
                        select c.*
                        from
                        (select b.kanji_collection_id,b.kanji_id,a.user_id,a.created_at,a.updated_at from
                        (select * from kanji_collections where user_id = :userId) a right outer join
                        (select * from kanji_collections where user_id = 7) b
                        on a.kanji_id = b.kanji_id) c inner join
                        kanjis k on c.kanji_id = k.kanji_id
                        WHERE k.category LIKE %:category% and k.jlpt_rank LIKE %:jlptRank% and k.kanji LIKE %:kanjiSearch%
                        """, nativeQuery = true)
        List<KanjiCollection> findKanjiCollectionsByUserId(@Param("userId") Long userId,
                        @Param("category") String category,
                        @Param("jlptRank") String jlptRank, @Param("kanjiSearch") String kanjiSearch);
}
