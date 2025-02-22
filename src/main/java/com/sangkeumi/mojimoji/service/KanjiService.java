package com.sangkeumi.mojimoji.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.repository.KanjiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiService {

    private final KanjiRepository kanjiRepository;

    public boolean checkAnswer(String korOnyomi, String korKunyomi, Long kanjiId) {

        Optional<Kanji> temp = kanjiRepository.findById(kanjiId);

        Kanji kanji = null;

        if (temp.isPresent()) {
            kanji = temp.get();
            log.info("==={}", kanji);

            boolean result = kanji.getKorOnyomi().equals(korOnyomi) && kanji.getKorKunyomi().equals(korKunyomi);
            return result;
        }

        return false;
    }

    public Kanji getKanji(Long kanjiId) {
        Optional<Kanji> temp = kanjiRepository.findById(kanjiId);

        Kanji kanji = null;

        if (temp.isPresent()) {
            kanji = temp.get();
            return kanji;
        }

        return null;
    }

}
