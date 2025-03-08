package com.sangkeumi.mojimoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.BookLine;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.UsedBookKanji;

public interface UsedBookKanjiRepository extends JpaRepository<UsedBookKanji, Long> {
    boolean existsByBookLineAndKanji(BookLine bookLine, Kanji kanji);
}
