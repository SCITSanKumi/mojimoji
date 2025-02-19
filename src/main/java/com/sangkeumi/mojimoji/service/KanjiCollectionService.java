package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.repository.KanjiCollectionsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiCollectionService {

    private final KanjiCollectionsRepository kanjiCollectionsRepository;

    @Transactional
    public boolean addCollection(KanjiCollection kanji) {

        KanjiCollection kanjiCollection = KanjiCollection.builder()
                .kanjiCollectionId(kanji.getKanjiCollectionId())
                .kanji(kanji.getKanji())
                .user(kanji.getUser())
                .createdAt(kanji.getCreatedAt())
                .updatedAt(kanji.getUpdatedAt())
                .build();

        log.info("한자컬렉션{}", kanjiCollection.toString());
        log.info("한자컬렉션{}", kanjiCollection.getKanjiCollectionId());
        log.info("한자컬렉션{}", kanjiCollection.getKanji().toString());
        log.info("한자컬렉션{}", kanjiCollection.getUser().toString());
        log.info("한자컬렉션{}", kanjiCollection.getCreatedAt());
        log.info("한자컬렉션{}", kanjiCollection.getUpdatedAt());

        kanjiCollectionsRepository.save(kanjiCollection);
        return true;
    }

}
