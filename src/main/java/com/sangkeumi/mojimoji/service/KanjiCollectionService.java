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

    }

}
