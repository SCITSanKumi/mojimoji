package com.sangkeumi.mojimoji.dto.rank;

public record BookRanking(
        Long userId,
        String nickname,
        String profileUrl,
        Long books,
        Long bookLines,
        Double bookScore // (books * 5 + bookLines * 1)
) {
}