package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sangkeumi.mojimoji.entity.Book;
import com.sangkeumi.mojimoji.entity.UsedBookKanji;

public interface UsedBookKanjiRepository extends JpaRepository<UsedBookKanji, Long> {
    @Query("SELECT ubk FROM UsedBookKanji ubk JOIN ubk.bookLine bl WHERE bl.book = :book")
    List<UsedBookKanji> findByBook(@Param("book") Book book);
}
