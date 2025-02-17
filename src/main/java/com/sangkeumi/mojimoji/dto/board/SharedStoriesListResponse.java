package com.sangkeumi.mojimoji.dto.board;

public record SharedStoriesListResponse(
        Long bookId,
        String title,
        String thumbnailUrl,
        String nickname,
        String profileUrl,
        int hitCount,
        int gaechu) {

}
