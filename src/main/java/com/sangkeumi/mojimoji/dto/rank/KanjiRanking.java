package com.sangkeumi.mojimoji.dto.rank;

public record KanjiRanking(
        Long userId,
        String nickname,
        String profileUrl,
        Long kanjiCollections,
        Long kanjiCollectionBonus,
        Double kanjiScore, // (kanjiCollections * 2 + kanjiCollectionBonus * 1)
        Double collectionPercentage // (kanjiCollections * 100 / TOTAL_KANJI_COUNT)
) {
}
