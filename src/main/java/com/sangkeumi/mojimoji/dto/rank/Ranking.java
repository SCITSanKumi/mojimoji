package com.sangkeumi.mojimoji.dto.rank;

public record Ranking(
                Long userId,
                String nickname,
                String profileUrl,
                Long books,
                Long bookLines,
                Long kanjiCollections,
                Long kanjiCollectionBonus,
                Long sharedBooks,
                Long totalHit,
                Long totalGaechu,
                Long sharedBookReplies,
                Double bookScore, // (Books*5 + BookLines*1)
                Double kanjiScore, // (KanjiCollections*2 + KanjiCollectionBonus*1)
                Double likeScore, // (SharedBooks*4 + TotalHit/100 + TotalGaechu*2 + SharedBookReplies*2)
                Double totalScore, // bookScore + kanjiScore + likeScore
                Double collectionPercentage // (kanjiCollections * 100 / TOTAL_KANJI_COUNT)
) {

}
