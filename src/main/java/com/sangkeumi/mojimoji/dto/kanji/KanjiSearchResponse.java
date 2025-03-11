package com.sangkeumi.mojimoji.dto.kanji;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KanjiSearchResponse {
    private Long kanjiId;
    private String kanji;
    private String jlptRank;
    private String category;
    private String korOnyomi;
    private String korKunyomi;
    private String jpnOnyomi;
    private String jpnKunyomi;
    private String meaning;
    private int bookmarked;
    private int collectedCount;
    private LocalDateTime firstCollectedAt;
}
