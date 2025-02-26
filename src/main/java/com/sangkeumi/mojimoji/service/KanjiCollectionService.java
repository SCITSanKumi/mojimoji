package com.sangkeumi.mojimoji.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.mypage.JlptCollectionStats;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KanjiCollectionService {

    private final KanjiCollectionsRepository kanjiCollectionsRepository;
    private final UserRepository userRepository;
    private final KanjiRepository kanjiRepository;

    @Transactional
    public void addCollection(Long kanjiId, MyPrincipal principal) {
        // 1) kanjiId, userId로 DB에서 엔티티 조회
        Kanji kanji = kanjiRepository.findById(kanjiId)
            .orElseThrow(() -> new RuntimeException("한자가 존재하지 않습니다."));

        User user = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        // 2) DB에 저장
        kanjiCollectionsRepository.save(KanjiCollection.builder()
            .kanji(kanji) // Kanji 엔티티
            .user(user) // User 엔티티
            .build()
        );
    }

    public List<JlptCollectionStats> getJlptStats(Long userId) {
        return kanjiCollectionsRepository.findJlptStatsByUserId(userId);
    }

}
