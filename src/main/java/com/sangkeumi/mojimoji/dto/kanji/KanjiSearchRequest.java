package com.sangkeumi.mojimoji.dto.kanji;

public record KanjiSearchRequest(
    String category,
    String jlptRank,
    String kanjiSearch,
    String kanjiSort,
    String sortDirection) {
    public KanjiSearchRequest(
            String category,
            String jlptRank,
            String kanjiSearch,
            String kanjiSort,
            String sortDirection) {
        // 값이 null이거나 공백이면 null로 처리 (검색 조건이 없다고 간주)
        this.category = (category == null || category.isBlank()) ? null : category;
        this.jlptRank = (jlptRank == null || jlptRank.isBlank()) ? null : jlptRank;
        this.kanjiSearch = (kanjiSearch == null || kanjiSearch.isBlank()) ? null : kanjiSearch;
        // 정렬 조건은 기본값 적용
        this.kanjiSort = (kanjiSort == null || kanjiSort.isBlank()) ? "kanjiId" : kanjiSort;
        this.sortDirection = (sortDirection == null || sortDirection.isBlank()) ? "asc" : sortDirection;
    }
}