package com.sangkeumi.mojimoji.dto.user;

import org.springframework.web.multipart.MultipartFile;

public record UserUpdateRequest(
    String nickname,
    String email,
    MultipartFile profileImageFile
) {
}