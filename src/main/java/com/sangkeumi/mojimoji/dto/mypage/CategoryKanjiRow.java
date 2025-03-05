package com.sangkeumi.mojimoji.dto.mypage;

public interface CategoryKanjiRow {
    String getCategory(); // category (문자열)
    String getJlptRank(); // JLPT 등급 (예: "N1", "N2", "N3", "N4", "N5" 등)
    Long getKanjiId(); // 한자의 PK (kanji_id)
    String getKanji(); // 한자 자체 (예: "日", "人" 등)
    Integer getIsCollected(); // 수집 여부 (1이면 수집됨, 0이면 미수집)
}
