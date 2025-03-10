package com.sangkeumi.mojimoji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.BookLine;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.UsedBookKanji;

public interface UsedBookKanjiRepository extends JpaRepository<UsedBookKanji, Long> {
    List<UsedBookKanji> findByBookLineAndKanjiIn(BookLine bookLine, List<Kanji> kanjiList);
}
