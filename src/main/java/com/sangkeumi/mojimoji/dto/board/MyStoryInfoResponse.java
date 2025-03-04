package com.sangkeumi.mojimoji.dto.board;

import lombok.Builder;

@Builder
public record MyStoryInfoResponse(
    Long sharedBookId,
    Long bookId,
    Long userId,
    String title,
    String thumbnailUrl,
    String nickname,
    String profileUrl,
    int hitCount,
    int gaechu) {
}
