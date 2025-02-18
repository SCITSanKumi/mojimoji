package com.sangkeumi.mojimoji.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.user.CustomOAuth2User;
import com.sangkeumi.mojimoji.entity.SocialAccount;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.entity.User.UserStatus;
import com.sangkeumi.mojimoji.repository.SocialAccountRepository;
import com.sangkeumi.mojimoji.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CustomOAuth2UserService
 *
 * 스프링 시큐리티 OAuth2 로그인 시, UserInfo 요청을 커스텀 처리하는 클래스.
 * 
 * 1) 소셜에서 받은 사용자 정보(OAuth2UserRequest, OAuth2User)를 바탕으로
 * DB에 User, SocialAccount 엔티티를 생성 또는 연동.
 * 2) 이메일이 이미 존재하는지 검사하여, 기존 User와 연결할지,
 * 새 User를 생성할지 결정.
 * 3) 최종적으로 CustomOAuth2User를 반환하여 SecurityContext에 저장.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // User, SocialAccount 테이블과 연동하기 위한 레포지토리
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    /**
     * OAuth2UserService<OAuth2UserRequest, OAuth2User> 구현 메서드.
     * 
     * @Transactional - DB 연동(INSERT/UPDATE) 로직이 있으므로 트랜잭션 범위 지정.
     */
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        /**
         * 1) DefaultOAuth2UserService를 통해 기본적인 사용자 정보(UserInfo)를 가져옴.
         * - Access Token으로 소셜 API 호출
         * - 반환된 JSON -> OAuth2User(기본 구현체: DefaultOAuth2User)
         */
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        /**
         * 2) provider, providerUserId, attributes 추출
         * - provider: 어떤 소셜인지 (예: "google", "kakao")
         * - providerUserId: 보통 "sub" 필드(구글 OIDC의 고유 식별자)
         * - attributes: 소셜에서 내려준 사용자 정보 (ex: {sub=..., email=..., name=...})
         */
        String provider = userRequest.getClientRegistration().getRegistrationId(); // ex: "google"
        String providerUserId = oAuth2User.getName(); // 구글 OIDC일 경우 sub 필드
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("=== OAuth2 Login ===");
        log.info("provider = {}", provider);
        log.info("providerUserId = {}", providerUserId);
        log.info("attributes = {}", attributes);

        /**
         * 3) 소셜 계정(SocialAccount) 존재 여부 확인
         * - (provider, providerUserId)로 DB 조회
         */
        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);

        User userEntity;

        if (socialAccount == null) {
            // 소셜 계정이 DB에 없음 → 새로 등록할지, 기존 이메일 유저와 연결할지 결정
            log.info("소셜 계정 (provider={}, providerUserId={})이 DB에 없음 → 신규 등록 흐름", provider, providerUserId);

            // 소셜 프로필에서 이메일 추출
            String email = (String) attributes.get("email");

            // 소셜 전용 username/password (NOT NULL 제약이 있는 경우 더미값 필요)
            String socialUsername = provider + "_" + providerUserId;
            String socialPassword = "$2a$10$e/7N06Z5kEOgjoIiQ91NhuIAIfe4rVEmGD/JnQDzVFuYGegZmJMii";

            // 3-1) 이메일로 기존 User가 있는지 확인
            User existingUser = userRepository.findByEmail(email).orElse(null);

            if (existingUser != null) {
                // [A] 기존 이메일 사용자 있음 → 소셜 계정만 연결
                log.info("이메일 '{}'을 가진 기존 유저(user_id={})와 소셜 계정 연동", email, existingUser.getUserId());

                existingUser.setStatus(UserStatus.ACTIVE);

                // 새 SocialAccount 엔티티 생성 → 기존 User에 연결
                SocialAccount newSocial = new SocialAccount();
                newSocial.setProvider(provider);
                newSocial.setProviderUserId(providerUserId);
                newSocial.setUser(existingUser);
                socialAccountRepository.save(newSocial);

                log.info("기존 User와 연결된 새로운 SocialAccount 생성: provider={}, providerUserId={}, social_account_id={}",
                        provider, providerUserId, newSocial.getSocialAccountId());

                userEntity = existingUser;

            } else {
                // [B] 기존 이메일 사용자 없음 → 새 User + SocialAccount 생성
                log.info("이메일 '{}'을 가진 유저가 없음 → 새로 User + SocialAccount 생성", email);

                // 새 User 엔티티 생성
                User newUser = new User();
                newUser.setNickname(socialUsername); // 닉네임은 "google_sub" 형태
                newUser.setUsername(providerUserId); // username은 sub(고유ID) 등
                newUser.setEmail(email);
                newUser.setPassword(socialPassword);
                newUser.setStatus(UserStatus.ACTIVE);

                userRepository.save(newUser);
                log.info("새로운 User 생성: user_id={}, nickname={}, email={}",
                        newUser.getUserId(), newUser.getNickname(), newUser.getEmail());

                // 소셜 계정도 연결
                SocialAccount newSocial = new SocialAccount();
                newSocial.setProvider(provider);
                newSocial.setProviderUserId(providerUserId);
                newSocial.setUser(newUser);
                socialAccountRepository.save(newSocial);

                log.info("새로운 SocialAccount 생성: social_account_id={}, provider={}, providerUserId={}",
                        newSocial.getSocialAccountId(), provider, providerUserId);

                userEntity = newUser;
            }

        } else {
            // 이미 존재하는 소셜 계정 → 해당 userEntity 재사용
            log.info("소셜 계정이 이미 DB에 존재 → user_id={}", socialAccount.getUser().getUserId());
            userEntity = socialAccount.getUser();
        }

        /**
         * 4) 스프링 시큐리티에서 사용할 OAuth2User 객체 반환
         * - CustomOAuth2User: 소셜 attributes + DB User 엔티티를 함께 보관
         * - SecurityContext에 저장되어, 인증된 Principal로 동작
         */
        return new CustomOAuth2User(userEntity, oAuth2User.getAttributes());
    }
}
