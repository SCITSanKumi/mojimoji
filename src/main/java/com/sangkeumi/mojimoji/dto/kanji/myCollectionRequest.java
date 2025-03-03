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

    Integer getCollectionCount();

    LocalDateTime getFirstCollectedAt();

    LocalDateTime getLastCollectedAt();
}