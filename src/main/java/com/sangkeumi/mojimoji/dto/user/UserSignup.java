package com.sangkeumi.mojimoji.dto.user;

public record UserSignup(
    String nickname,
    String username,
    String email,
    String password
) {
}
