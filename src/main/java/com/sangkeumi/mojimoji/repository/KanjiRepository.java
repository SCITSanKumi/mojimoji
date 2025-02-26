package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {
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
