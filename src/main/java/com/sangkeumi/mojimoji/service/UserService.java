package com.sangkeumi.mojimoji.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // UserSignup DTO로부터 User 엔티티 생성
    public boolean signUp(UserSignup userSignup) {
        User entity = User.builder()
                .nickname(userSignup.nickname()) // nickname 필드
                .username(userSignup.username()) // username 필드
                .password(bCryptPasswordEncoder.encode(userSignup.password())) // password 필드
                .email(userSignup.email()) // email 필드
                .build();

        try {
            // DB 저장
            userRepository.save(entity);
            // 저장이 성공적으로 끝나면 true 반환
            return true;
        } catch (Exception e) {
            // DB 저장 과정에서 예외가 발생하면 false 반환 (예: 제약조건 위반 등)
            // 필요하다면 로그 남기기
            // log.error("회원가입 중 오류 발생", e);
            return false;
        }

    }

    public boolean existByUsername(String username) {
        // 중복된 아이디 있는지 확인 있으면 true 없으면 false 반환
        boolean result = userRepository.existsByUsername(username);
        // 중복된 아이디가 있다면 false 반환 없다면 true 반환
        return !result;
    }

    public boolean existByNickname(String nickname) {
        boolean result = userRepository.existsByNickname(nickname);
        return !result;
    }

    public boolean existByEmail(String email) {
        boolean result = userRepository.existsByEmail(email);
        return !result;
    }

}
