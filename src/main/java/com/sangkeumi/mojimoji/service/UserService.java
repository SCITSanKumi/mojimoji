package com.sangkeumi.mojimoji.service;

import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.user.UserSignup;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // UserSignup DTO로부터 User 엔티티 생성
    public boolean signUp(UserSignup userSignup) {
        User entiry = User.builder()
                .nickname(userSignup.nickname()) // nickname 필드
                .username(userSignup.username()) // username 필드
                .password(userSignup.password()) // password 필드
                .email(userSignup.email()) // email 필드
                .build();

        // username이 이미 존재하는지 확인
        boolean result = userRepository.existsByUsername(userSignup.username());
        if (result) {
            // 이미 존재하는 경우 false 반환
            return false;
        }
        // 존재하지 않으면 새로 저장
        userRepository.save(entiry);
        // 저장 후 true 반환
        return !result;

    }

    public boolean existByUsername(String username) {
        // 중복된 닉네임 있는지 확인 있으면 true 없으면 false 반환
        boolean result = userRepository.existsByUsername(username);
        // 중복된 아이디가 있다면 false 반환 없다면 true 반환
        return !result;
    }

}
