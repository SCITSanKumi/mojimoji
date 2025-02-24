package com.sangkeumi.mojimoji.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.kanji.AddKanjiCollection;
import com.sangkeumi.mojimoji.dto.mypage.JlptCollectionStats;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.KanjiCollectionsRepository;
import com.sangkeumi.mojimoji.repository.KanjiRepository;
import com.sangkeumi.mojimoji.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiCollectionService {

    private final KanjiCollectionsRepository kanjiCollectionsRepository;
    private final UserRepository userRepository;
    private final KanjiRepository kanjiRepository;

    @Transactional
    public boolean addCollection(AddKanjiCollection addKanjiCollection) {
        try {
            // 1) kanjiId, userId로 DB에서 엔티티 조회
            Kanji kanji = kanjiRepository.findById(addKanjiCollection.kanjiId())
                    .orElseThrow(() -> new RuntimeException("Kanji not found"));

            User user = userRepository.findById(addKanjiCollection.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2) KanjiCollection 엔티티 생성
            KanjiCollection kanjiCollection = KanjiCollection.builder()
                    .kanji(kanji) // Kanji 엔티티
                    .user(user) // User 엔티티
                    .build();

            // 3) DB에 저장
            kanjiCollectionsRepository.save(kanjiCollection);

            return true;
        } catch (Exception e) {
            // 예외 발생 시 false 반환 (필요하다면 로그 남기기)
            return false;
        }
    }

    public List<JlptCollectionStats> getJlptStats(Long userId) {
        return kanjiCollectionsRepository.findJlptStatsByUserId(userId);
    }

}
