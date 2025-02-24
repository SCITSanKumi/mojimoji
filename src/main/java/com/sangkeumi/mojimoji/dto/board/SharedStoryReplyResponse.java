package com.sangkeumi.mojimoji.dto.board;

import java.time.LocalDateTime;

public record SharedStoryReplyResponse(
    Long sharedBookReplyId,
    Long sharedBookId,
    Long userId,
    String nickname,
    String content,
    LocalDateTime createdAt) {
}
