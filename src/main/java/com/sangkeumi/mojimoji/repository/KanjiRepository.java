package com.sangkeumi.mojimoji.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sangkeumi.mojimoji.entity.Kanji;

public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    Optional<Kanji> findByKanji(String kanji);
    List<Kanji> findByKanjiIn(Set<String> kanjiSet);
}
