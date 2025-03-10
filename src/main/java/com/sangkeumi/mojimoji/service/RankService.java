package com.sangkeumi.mojimoji.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.rank.*;
import com.sangkeumi.mojimoji.repository.RankingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankingRepository rankingRepository;

    public List<Ranking> getOverallRankings() {
        return rankingRepository.findOverallRankingRecords();
    }

    public List<KanjiRanking> getKanjiRankings() {
        return rankingRepository.findKanjiRankingRecords();
    }

    public List<BookRanking> getBookRankings() {
        return rankingRepository.findBookRankingRecords();
    }

    public List<LikeRanking> getLikesRankings() {
        return rankingRepository.findLikesRankingRecords();
    }

}
