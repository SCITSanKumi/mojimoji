package com.sangkeumi.mojimoji.dto.board;

import java.time.LocalDateTime;

public record MyStoryListResponse(
        Long bookId,
        String title,
        String thumbnailUrl,
        boolean isEnded,
        boolean isShared,
        String nickname,
        String profileUrl,
        int hitCount,
        int gaechu,
        LocalDateTime createdAt) {
}
