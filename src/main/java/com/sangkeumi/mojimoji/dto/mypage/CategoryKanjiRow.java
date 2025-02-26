package com.sangkeumi.mojimoji.dto.mypage;

public interface CategoryKanjiRow {
    // category (문자열)
    String getCategory();

    // JLPT 등급 (예: "N1", "N2", "N3", "N4", "N5" 등)
    String getJlptRank();

    // 한자의 PK (kanji_id)
    Long getKanjiId();

    // 한자 자체 (예: "日", "人" 등)
    String getKanji();

    // 수집 여부 (1이면 수집됨, 0이면 미수집)
    Integer getIsCollected();
}
