package com.sangkeumi.mojimoji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.KanjiCollectionRepository;
import com.sangkeumi.mojimoji.repository.KanjiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiCollectionService {

    private final KanjiCollectionRepository kanjiCollectionRepository;
    private final KanjiRepository kanjiRepository;

    public List<KanjiCollection> getKanjiCollection(Long userId, String category, String jlptRank, String kanjiSearch) {

        // List<Kanji> kanjiList =
        // kanjiRepository.findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(category,
        // jlptRank,
        // kanjiSearch);

        // List<KanjiCollection> kanjiCollection = new ArrayList<>();

        // for (Kanji kanji : kanjiList) {
        // Optional<KanjiCollection> temp =
        // kanjiCollectionRepository.findByUserAndKanji(user, kanji);

        // if (temp.isPresent()) {
        // kanjiCollection.add(temp.get());
        // }
        // }
        // Long userId = user.getUserId();
        List<KanjiCollection> kanjiCollection = kanjiCollectionRepository.findKanjiCollectionsByUserId(userId, category,
                jlptRank, kanjiSearch);
        return kanjiCollection;
    }

    public List<KanjiCollection> getMyCollection(User user, String category,
            String jlptRank,
            String kanjiSearch) {
        Long userId = user.getUserId();
        List<KanjiCollection> kanjiCollection = kanjiCollectionRepository.findKanjiCollectionsByUserId(userId, category,
                jlptRank, kanjiSearch);

        List<KanjiCollection> myCollection = new ArrayList<>();

        // log.info("kanji = {}", kanjiCollection.get(99).getKanji().getKanji());
        for (KanjiCollection kanji : kanjiCollection) {
            if (kanji != null) {
                if (kanji.getUser() == user) {
                    myCollection.add(kanji);
                }
            }
        }

        return myCollection;
    }

    // 백업
    public List<KanjiCollection> getAllKanjiCollection(String category, String jlptRank, String kanjiSearch,
            User notUser) {

        List<Kanji> allKanjiList = kanjiRepository.findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(
                category,
                jlptRank,
                kanjiSearch);

        List<KanjiCollection> allKanjiCollection = new ArrayList<>();

        for (Kanji kanji : allKanjiList) {
            KanjiCollection temp = KanjiCollection.builder()
                    .kanji(kanji)
                    .user(notUser)
                    .build();

            allKanjiCollection.add(temp);
        }
        log.info("allKanjiCollection: {}", allKanjiCollection.size());
        return allKanjiCollection;
    }

}
