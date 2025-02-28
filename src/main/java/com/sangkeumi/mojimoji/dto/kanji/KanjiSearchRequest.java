package com.sangkeumi.mojimoji.dto.kanji;

public record KanjiSearchRequest(
        String category,
        String jlptRank,
        String kanjiSearch,
        String kanjiSort,
        String sortDirection
) {
    public KanjiSearchRequest(
            String category,
            String jlptRank,
            String kanjiSearch,
            String kanjiSort,
            String sortDirection) {
        this.category = category != null ? category : "";
        this.jlptRank = jlptRank != null ? jlptRank : "";
        this.kanjiSearch = kanjiSearch != null ? kanjiSearch : "";
        this.kanjiSort = kanjiSort != null ? kanjiSort : "한자번호순";
        this.sortDirection = sortDirection != null ? sortDirection : "오름차순";
    }
}