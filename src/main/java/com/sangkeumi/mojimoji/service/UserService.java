package com.sangkeumi.mojimoji.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sangkeumi.mojimoji.dto.user.*;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginUserDetailsService loginUserDetailsService;

    @Value("${upload.path.image}")
    private String imagePath;

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

    @Transactional
    public CustomUser getUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // 여기서 user.getNickname() 등 Lazy 필드 접근 가능 (트랜잭션 내)
        return new CustomUser(user.getUserId(), user.getNickname(), user.getEmail());
    }

    public User getUser(Long userId) {

        Optional<User> temp = userRepository.findById(userId);

        if (temp.isPresent()) {
            User user = temp.get();
            return user;
        }
        return null;
    }

    public UserUpdateView findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for id: " + userId));

        return new UserUpdateView(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getEmail(),
                user.getSocialAccounts() != null && !user.getSocialAccounts().isEmpty());
    }

    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 중복 닉네임 체크 (자신의 닉네임은 제외)
        if (!user.getNickname().equals(userUpdateRequest.nickname()) &&
                !existByNickname(userUpdateRequest.nickname())) {
            throw new IllegalArgumentException("다른 유저가 사용중인 닉네임입니다.");
        }

        // 중복 이메일 체크 (자신의 이메일은 제외)
        if (!user.getEmail().equals(userUpdateRequest.email()) &&
                !existByEmail(userUpdateRequest.email())) {
            throw new IllegalArgumentException("다른 유저가 사용중인 이메일입니다.");
        }
        // 사용자 정보 업데이트
        user.setNickname(userUpdateRequest.nickname());
        user.setEmail(userUpdateRequest.email());

        MultipartFile profileImageFile = userUpdateRequest.profileImageFile();
        long maxFileSize = 100 * 1024 * 1024; // 100MB 제한

        try {
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
            if (profileImageFile.getSize() > maxFileSize) {
                throw new IllegalArgumentException("파일 크기가 100MB를 초과할 수 없습니다.");
            }

            // 저장될 디렉토리 확인 및 생성 (imagesPath는 @Value로 주입받음)
            Path dirPath = Paths.get(imagePath, "profile_images");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 고유 파일명 생성 후 파일 저장
            String fileName = UUID.randomUUID().toString() + "_" + profileImageFile.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);
            Files.copy(profileImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 웹 접근 가능한 상대 URL로 설정 (예: /images/profile_images/파일명)
            user.setProfileUrl("/image/profile_images/" + fileName);
            }

            // 변경된 사용자 정보를 DB에 저장
            userRepository.save(user);

            // 재인증 (필요한 경우)
            reAuthenticateUser(user.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("프로필 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    private void reAuthenticateUser(String username) {
        // 3-1) 변경된 사용자 정보를 기반으로 UserDetails 다시 생성
        UserDetails updatedUserDetails = loginUserDetailsService.loadUserByUsername(username);

        // 3-2) 기존 Authentication 가져오기
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        // 3-3) 새로운 Authentication 생성
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                currentAuth.getCredentials(),
                updatedUserDetails.getAuthorities());

        // 3-4) SecurityContextHolder 업데이트
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Transactional
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            // 현재 비밀번호 확인
            if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            return true;
        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            log.error("계정 삭제 실패", e);
            return false;
        }
    }

    public String getUserProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user.getProfileUrl();
    }
}
