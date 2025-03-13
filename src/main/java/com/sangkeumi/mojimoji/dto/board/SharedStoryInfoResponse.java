package com.sangkeumi.mojimoji.dto.board;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record SharedStoryInfoResponse(
    Long sharedBookId,
    Long bookId,
    Long userId,
    String title,
    String thumbnailUrl,
    String nickname,
    String profileUrl,
    int hitCount,
    int gaechu,
    LocalDateTime createdAt,
    boolean liked) {
}
