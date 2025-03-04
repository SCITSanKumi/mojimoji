package com.sangkeumi.mojimoji.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.kanji.KanjiCollectionSummaryId;
import com.sangkeumi.mojimoji.dto.kanji.KanjiCount;
import com.sangkeumi.mojimoji.dto.kanji.KanjiSearchRequest;
import com.sangkeumi.mojimoji.dto.kanji.myCollectionRequest;
import com.sangkeumi.mojimoji.entity.Kanji;
import com.sangkeumi.mojimoji.entity.KanjiCollection;
import com.sangkeumi.mojimoji.entity.KanjiCollectionSummary;
import com.sangkeumi.mojimoji.entity.User;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sangkeumi.mojimoji.dto.mypage.*;
import com.sangkeumi.mojimoji.dto.user.MyPrincipal;
import com.sangkeumi.mojimoji.repository.*;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class KanjiCollectionService {

    private final KanjiCollectionsRepository kanjiCollectionsRepository;
    private final UserRepository userRepository;
    private final KanjiRepository kanjiRepository;
    private final KanjiCollectionSummaryRepository kanjiCollectionSummaryRepository;

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
                .build());

        // 3) 요약 테이블(Kanji_Collection_Summary) 업데이트
        // 복합키를 구성하기 위한 임베디드 ID 생성
        KanjiCollectionSummaryId summaryId = new KanjiCollectionSummaryId(
                user.getUserId(),
                kanji.getKanjiId());

        // (a) 요약 레코드가 이미 존재하는지 확인
        Optional<KanjiCollectionSummary> optionalSummary = kanjiCollectionSummaryRepository.findById(summaryId);

        if (optionalSummary.isPresent()) {
            // (b) 이미 있으면 collection_count + 1, 마지막 수집 시각 갱신
            KanjiCollectionSummary summary = optionalSummary.get();
            summary.setCollectionCount(summary.getCollectionCount() + 1);

            if (summary.getFirstCollectedAt() == null) {
                summary.setFirstCollectedAt(LocalDateTime.now());
            }
            summary.setLastCollectedAt(LocalDateTime.now());

            kanjiCollectionSummaryRepository.save(summary);

        } else {
            // (c) 없으면 새로 생성 (collection_count=1, first/last_collected_at=now)
            KanjiCollectionSummary summary = new KanjiCollectionSummary();
            summary.setId(summaryId);
            summary.setUser(user); // @MapsId("userId")와 매핑
            summary.setKanji(kanji); // @MapsId("kanjiId")와 매핑
            summary.setCollectionCount(1);
            summary.setFirstCollectedAt(LocalDateTime.now());
            summary.setLastCollectedAt(LocalDateTime.now());

            kanjiCollectionSummaryRepository.save(summary);
        }
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

    /**
     * 무한 스크롤: page 당 10개 조회
     */
    public List<myCollectionRequest> getMyCollection(KanjiSearchRequest searchRequest, Long userId, int page) {
        // fallback
        String sortType = searchRequest.kanjiSort();
        if (!"한자번호순".equals(sortType) && !"최근등록순".equals(sortType)) {
            sortType = "한자번호순";
        }
        String sortDir = searchRequest.sortDirection();
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            sortDir = "asc";
        }

        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        String category = searchRequest.category();
        String jlptRank = searchRequest.jlptRank();
        String searchTerm = searchRequest.kanjiSearch();

        // 정렬 분기
        if ("한자번호순".equals(sortType)) {
            // 한자번호순
            if ("desc".equalsIgnoreCase(sortDir)) {
                return kanjiRepository.findKanjiOrderByKanjiIdDesc(userId, category, jlptRank, searchTerm, pageSize,
                        offset);
            } else {
                return kanjiRepository.findKanjiOrderByKanjiIdAsc(userId, category, jlptRank, searchTerm, pageSize,
                        offset);
            }
        } else {
            // 최근등록순
            if ("desc".equalsIgnoreCase(sortDir)) {
                return kanjiRepository.findKanjiOrderByCollectedDesc(userId, category, jlptRank, searchTerm, pageSize,
                        offset);
            } else {
                return kanjiRepository.findKanjiOrderByCollectedAsc(userId, category, jlptRank, searchTerm, pageSize,
                        offset);
            }
        }
    }

    public KanjiCount findTotalAndCollected(KanjiSearchRequest req, Long userId) {
        return kanjiCollectionsRepository.findTotalAndCollected(
                userId,
                req.category(),
                req.jlptRank(),
                req.kanjiSearch());
    }
}
