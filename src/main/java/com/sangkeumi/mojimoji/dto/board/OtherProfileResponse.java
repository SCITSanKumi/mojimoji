package com.sangkeumi.mojimoji.dto.board;

public record OtherProfileResponse(
    Long userId,
    String nickname,
    String email,
    String profileUrl) {
}
