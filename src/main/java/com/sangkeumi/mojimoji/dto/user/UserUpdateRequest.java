package com.sangkeumi.mojimoji.dto.user;

public record UserUpdateRequest(
    String nickname,
    String email
) {
}