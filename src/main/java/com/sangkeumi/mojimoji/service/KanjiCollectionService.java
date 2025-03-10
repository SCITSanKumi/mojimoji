package com.sangkeumi.mojimoji.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sangkeumi.mojimoji.dto.kanji.*;
import com.sangkeumi.mojimoji.dto.mypage.*;
import com.sangkeumi.mojimoji.entity.*;
import com.sangkeumi.mojimoji.repository.*;

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

        if (kanjiCollection.getCollectedCount() == 0 && kanjiCollection.getWrongCount() >= 1) {
            kanjiCollection.setCreatedAt(LocalDateTime.now());
        }

        kanjiCollection.setCollectedCount(kanjiCollection.getCollectedCount() + 1);

        kanjiCollectionsRepository.save(kanjiCollection);
    }

    @Transactional
    public void wrongCountUp(Long kanjiId, Long userId) {
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
                        .wrongCount(0)
                        .build());

        kanjiCollection.setWrongCount(kanjiCollection.getWrongCount() + 1);

        kanjiCollectionsRepository.save(kanjiCollection);
    }

    public Page<KanjiSearchResponse> getMyCollection(Long userId, KanjiSearchRequest searchRequest, int page) {

        Sort sort = Sort.by(
                searchRequest.sortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                searchRequest.kanjiSort());

        return kanjiRepository.findMyCollection(userId, searchRequest.category(), searchRequest.jlptRank(),
                searchRequest.kanjiSearch(),
                PageRequest.of(page - 1, 10, sort));
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

    public KanjiCount findTotalAndCollected(KanjiSearchRequest req, Long userId) {
        return kanjiCollectionsRepository.findTotalAndCollected(
                userId,
                req.category(),
                req.jlptRank(),
                req.kanjiSearch());
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

    public List<WrongKanji> getWrongKanji(Long userId) {

        return kanjiCollectionsRepository.findAllByUserId(userId);

    }

    @Transactional
    public void wrongDelete(Long kanjiId, Long userId) {

        Kanji kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("한자가 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Optional<KanjiCollection> temp = kanjiCollectionsRepository.findByKanjiAndUser(kanji, user);

        if (temp.isPresent()) {
            temp.get().setWrongCount(0);
        }
    }

    @Transactional
    public void addBookMark(Long kanjiId, Long userId) {
        Kanji kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("한자가 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Optional<KanjiCollection> temp = kanjiCollectionsRepository.findByKanjiAndUser(kanji, user);

        if (temp.isPresent()) {
            temp.get().setBookmarked(1);
        }
    }

    @Transactional
    public void deleteBookMark(Long kanjiId, Long userId) {
        Kanji kanji = kanjiRepository.findById(kanjiId)
                .orElseThrow(() -> new RuntimeException("한자가 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        Optional<KanjiCollection> temp = kanjiCollectionsRepository.findByKanjiAndUser(kanji, user);

        if (temp.isPresent()) {
            temp.get().setBookmarked(0);
        }
    }
}
