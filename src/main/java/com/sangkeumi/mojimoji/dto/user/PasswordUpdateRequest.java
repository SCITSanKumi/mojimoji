package com.sangkeumi.mojimoji.dto.user;

public record PasswordUpdateRequest(
    String currentPassword,
    String newPassword,
    String confirmNewPassword
) {
}