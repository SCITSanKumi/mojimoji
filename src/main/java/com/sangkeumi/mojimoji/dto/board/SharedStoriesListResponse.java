package com.sangkeumi.mojimoji.dto.board;

import java.time.LocalDateTime;

public record SharedStoriesListResponse(
    Long bookId,
    String title,
    String thumbnailUrl,
    String nickname,
    String profileUrl,
    int hitCount,
    int gaechu,
    LocalDateTime createdAt) {
}
