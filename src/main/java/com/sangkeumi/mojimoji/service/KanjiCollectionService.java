package com.sangkeumi.mojimoji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
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

    public List<myCollectionRequest> getMyCollection(Long userId, String category, String jlptRank,
            String kanjiSearch, String kanjiSort, String sortDirection) {

        switch (kanjiSort) {
            case "한자번호순":
                Integer Sort = 1;
                sortDirection = "";
                if (sortDirection == "오름차순") {
                    sortDirection = "asc";
                } else {
                    sortDirection = "desc";
                }
                return kanjiRepository.findKanjiCollectionStatusByUserId(userId, category,
                        jlptRank, kanjiSearch,
                        Sort, sortDirection);
            case "최근등록순":
                Sort = 11;
                if (sortDirection == "오름차순") {
                    sortDirection = "asc";
                } else {
                    sortDirection = "desc";
                }
                return kanjiRepository.findKanjiCollectionStatusByUserId(userId, category,
                        jlptRank, kanjiSearch,
                        Sort, sortDirection);
        }

        return kanjiRepository.findKanjiCollectionStatusByUserId(userId, category, jlptRank, kanjiSearch, 1,
                "asc");

        // List<KanjiCollection> kanjiCollection =
        // kanjiCollectionRepository.findKanjiCollectionsByUserId(userId, category,
        // jlptRank, kanjiSearch);
        // return kanjiCollection;
        // }

        // public List<KanjiCollection> getMyCollection(User user, String category,
        // String jlptRank,
        // String kanjiSearch) {
        // Long userId = user.getUserId();
        // List<KanjiCollection> kanjiCollection =
        // kanjiCollectionRepository.findKanjiCollectionsByUserId(userId, category,
        // jlptRank, kanjiSearch);

        // List<KanjiCollection> myCollection = new ArrayList<>();

        // for (KanjiCollection kanji : kanjiCollection) {
        // if (kanji != null) {
        // if (kanji.getUser() == user) {
        // myCollection.add(kanji);
        // }
        // }
        // }

    }

    // 백업
    // public List<KanjiCollection> getAllKanjiCollection(String category, String
    // jlptRank, String kanjiSearch,
    // User notUser) {

    // List<Kanji> allKanjiList =
    // kanjiRepository.findAllByCategoryContainsAndJlptRankContainsAndKanjiContains(
    // category,
    // jlptRank,
    // kanjiSearch);

    // List<KanjiCollection> allKanjiCollection = new ArrayList<>();

    // for (Kanji kanji : allKanjiList) {
    // KanjiCollection temp = KanjiCollection.builder()
    // .kanji(kanji)
    // .user(notUser)
    // .build();

    // allKanjiCollection.add(temp);
    // }
    // log.info("allKanjiCollection: {}", allKanjiCollection.size());
    // return allKanjiCollection;
    // }

}
