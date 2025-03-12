package com.sangkeumi.mojimoji.dto.kanji;

import java.time.LocalDateTime;

public interface WrongKanji {
    Long getKanjiCollectionId();
    Long getKanjiId();
    Long getUserId();
    int getBookmarked();
    Long getCollectedCount();
    Long getWrongCount();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getJlptRank();
    String getCategory();
    String getKanji();
    String getKorOnyomi();
    String getKorKunyomi();
    String getJpnOnyomi();
    String getJpnKunyomi();
    String getMeaning();
}
