package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.repository.KanjiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KanjiService {

    private final KanjiRepository kanjiRepository;

    public List<Kanji> getKanjiList(String category, String jlptRank, String kanjiSearch) {
        List<Kanji> kanjiList = kanjiRepository.findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(category,
                jlptRank,
                kanjiSearch);
        return kanjiList;
    }

    public Kanji getKanji(Long kanjiId) {

        Optional<Kanji> temp = kanjiRepository.findById(kanjiId);

        if (temp.isPresent()) {
            return temp.get();
        }
        return null;
    }
}
