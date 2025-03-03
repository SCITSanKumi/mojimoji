package com.sangkeumi.mojimoji.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sangkeumi.mojimoji.dto.kanji.*;
import com.sangkeumi.mojimoji.dto.mypage.*;
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
    public void addCollection(Long kanjiId, Long userId) {
        // 1) kanjiId, userId로 DB에서 엔티티 조회
        Kanji kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("한자가 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        // 2) DB에 저장
        KanjiCollection kanjiCollection = kanjiCollectionsRepository.findByKanjiAndUser(kanji, user)
            .orElse(KanjiCollection.builder()
                .kanji(kanji)
                .user(user)
                .collectedCount(0)
                .build());

        kanjiCollection.setCollectedCount(kanjiCollection.getCollectedCount() + 1);
        kanjiCollectionsRepository.save(kanjiCollection);
    }
    
    public List<KanjiSearchResponse> getMyCollection(KanjiSearchRequest searchRequest, Long userId) {
    
        Sort sort = Sort.by(
            searchRequest.sortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
            searchRequest.kanjiSort()
        );
    
        return kanjiRepository.findMyCollection(userId, searchRequest.category(), searchRequest.jlptRank(), searchRequest.kanjiSearch(), 
            PageRequest.of(0, Integer.MAX_VALUE, sort)).getContent();
    }
    
    /**
     * 카테고리별 한자 목록 + 수집 여부를 Map 형태로 반환.
     * key : category (String)
     * value : 해당 category에 속한 한자들(List<CategoryKanjiRow>)
     */
    public Map<String, List<CategoryKanjiRow>> getCategoryKanjiMap(Long userId) {
        // 1) DB에서 모든 한자 + 수집 여부 가져옴
        List<CategoryKanjiRow> rows = kanjiCollectionsRepository.findCategoryKanjiRows(userId);
        
        // 2) Java Stream으로 category별 grouping
        Map<String, List<CategoryKanjiRow>> grouped = rows.stream()
        .collect(Collectors.groupingBy(CategoryKanjiRow::getCategory));
        
        return grouped;
    }

    public List<JlptCollectionStats> getJlptStats(Long userId) {
        return kanjiCollectionsRepository.findJlptStatsByUserId(userId);
    }

    public List<DailyAcquisitionStats> getDailyStats(Long userId) {
        return kanjiCollectionsRepository.findDailyStatsByUserId(userId);
    }

    public Double getDailyAverage(Long userId) {
        return kanjiCollectionsRepository.findDailyAverageByUserId(userId);
    }

    public CategoryCollectionSummary getCategoryCollectionSummary(Long userId) {
        return kanjiCollectionsRepository.findCategoryCollectionSummary(userId);
    }
}
