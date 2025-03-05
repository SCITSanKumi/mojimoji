package com.sangkeumi.mojimoji.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.repository.KanjiRepository;

import lombok.RequiredArgsConstructor;

import com.sangkeumi.mojimoji.dto.kanji.KanjiDetailResponse;

import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.repository.KanjiCollectionsRepository;

import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiService {

    private final KanjiRepository kanjiRepository;
    private final KanjiCollectionsRepository kanjiCollectionsRepository;

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

    /**
     * 해당 한자의 상세 정보를 조회합니다.
     * - 한자 정보는 무조건 반환 (kanjis 테이블)
     * - 사용자가 해당 한자를 획득했으면 획득 날짜를, 아니면 "미수집" 표시
     * 
     * @param kanjiId 한자 ID
     * @param userId  사용자 ID
     * @return KanjiDetailResponse record
     */
    @Transactional
    public KanjiDetailResponse getKanjiDetail(Long kanjiId, Long userId) {
        // 1. Kanji 테이블에서 한자 정보 조회
        Kanji kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("Kanji not found"));

        // 2. Kanji_Collections에서 해당 한자를 사용자가 획득한 기록 조회 (최초 획득 날짜)
        Optional<KanjiCollection> collectionOpt = kanjiCollectionsRepository
                .findFirstByUserUserIdAndKanji_KanjiIdOrderByCreatedAtAsc(userId, kanjiId);

        LocalDateTime obtainedAt = collectionOpt.map(kc -> kc.getCreatedAt()).orElse(null);

        return new KanjiDetailResponse(
                kanji.getKanjiId(),
                kanji.getJlptRank(),
                kanji.getCategory(),
                kanji.getKanji(),
                kanji.getKorOnyomi(),
                kanji.getKorKunyomi(),
                kanji.getJpnOnyomi(),
                kanji.getJpnKunyomi(),
                kanji.getMeaning(),
                obtainedAt);
    }

}
