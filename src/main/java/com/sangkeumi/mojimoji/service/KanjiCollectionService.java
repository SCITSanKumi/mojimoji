package com.sangkeumi.mojimoji.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
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
        // 1. 고정 정렬 조건: collectedCount가 0보다 큰 항목을 우선 (컬렉션에 데이터가 있으면 0, 없으면 1로 정렬)
        Sort primarySort = JpaSort.unsafe("case when COALESCE(kc.collectedCount, 0) > 0 then 0 else 1 end").ascending();

        // 2. 사용자가 선택한 동적 정렬 조건
        Sort dynamicSort = Sort.by(searchRequest.sortDirection().equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC, searchRequest.kanjiSort());

        // 3. 두 정렬 조건 결합 (primarySort가 우선 적용됨)
        Sort sort = primarySort.and(dynamicSort);

        // 4. PageRequest 생성 (페이지 번호가 0 기반인지 확인)
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), 10, sort);

        return kanjiCollectionsRepository.findMyCollection(
                userId,
                searchRequest.category(),
                searchRequest.jlptRank(),
                searchRequest.kanjiSearch(),
                pageRequest
        );
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
                .findByUserUserIdAndKanji_KanjiId(userId, kanjiId);

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
        } else {
            // log.info("kc{}", temp.get().toString());
            // kanjiCollectionsRepository.save(temp.get());
            KanjiCollection kanjiCollection = kanjiCollectionsRepository.findByKanjiAndUser(kanji, user)
                    .orElse(KanjiCollection.builder()
                            .kanji(kanji)
                            .user(user)
                            .collectedCount(0)
                            .wrongCount(0)
                            .bookmarked(1)
                            .build());
            kanjiCollectionsRepository.save(kanjiCollection);
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

    public Set<QuizKanjiDTO> getKanjiQuiz(Long bookId, Long userId) {
        return new HashSet<>(kanjiCollectionsRepository.findKanjisToQuiz(bookId, userId));
    }

    public List<BookmarkedKanjiDTO> getBookmarkedKanji(Long userId) {
        return kanjiCollectionsRepository.findBookmarkedKanjis(userId);
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

    public int getBookmarkNum(Long kanjiId, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Kanji> kanji = kanjiRepository.findById(kanjiId);
        Optional<KanjiCollection> temp = kanjiCollectionsRepository.findByUserAndKanji(user.get(), kanji.get());

        if (temp.isPresent()) {
            log.info("bookkkkkkkkkkkkkkkk{}", temp.get().getBookmarked());
            return temp.get().getBookmarked();
        }
        return 0;
    }
}
