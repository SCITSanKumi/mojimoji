package com.sangkeumi.mojimoji.dto.user;

public record UserUpdateView(
    Long userId,
    String username,
    String password,
    String nickname,
    String email,
    boolean isSocialUser
) {
}
