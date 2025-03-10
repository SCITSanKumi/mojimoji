package com.sangkeumi.mojimoji.dto.rank;

public record LikeRanking(
        Long userId,
        String nickname,
        String profileUrl,
        Long sharedBooks,
        Long totalHit,
        Long totalGaechu,
        Long sharedBookReplies,
        Double likeScore // (sharedBooks * 4 + totalHit/100 + totalGaechu * 2 + sharedBookReplies * 2)
) {
}
