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
        this.kanjiSort = kanjiSort != null ? kanjiSort : "kanjiId";
        this.sortDirection = sortDirection != null ? sortDirection : "ASC";
    }
}