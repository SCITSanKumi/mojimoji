package com.sangkeumi.mojimoji.dto.kanji;

import java.time.LocalDateTime;

public interface myCollectionRequest {
    Long getKanjiId();

    String getKanji();

    String getJlptRank();

    String getCategory();

    String getKorOnyomi();

    String getKorKunyomi();

    String getJpnOnyomi();

    String getJpnKunyomi();

    String getMeaning();

    Integer getIsCollected();

    LocalDateTime getFirstCollectedAt();
    // Long kanjiId,
    // String kanji,
    // String jlptRank,
    // String category,
    // String korOnyomi,
    // String korKunyomi,
    // String jpnOnyomi,
    // String jpnKunyomi,
    // String meaning,
    // Integer isCollected,
    // LocalDateTime firstCollectedAt
}