package com.sangkeumi.mojimoji.dto.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.sangkeumi.mojimoji.entity.User;

/**
 * CustomOAuth2User
 *
 * Spring Security의 OAuth2User 인터페이스를 구현하여,
 * 소셜 로그인 후 사용자 정보를 보관하는 커스텀 클래스입니다.
 * 
 * - attributes: 소셜에서 내려준 사용자 정보(JSON)를 맵 형태로 저장.
 * - userEntity: 프로젝트의 User 엔티티(데이터베이스에 저장된 유저 정보).
 * 
 * 이 클래스를 통해 스프링 시큐리티는 인증된 사용자(Principal)를 인식하고,
 * SecurityContextHolder에 저장하게 됩니다.
 */
public class CustomOAuth2User implements OAuth2User, MyPrincipal {

    // ★ LazyInitializationException 방지용으로, 엔티티 대신 필드를 복사해 둠
    private final Long userId;
    private final String nickname;

    // 소셜 attributes (구글: {sub=..., email=..., name=...})
    private final Map<String, Object> attributes;

    // [선택] 필요하면 엔티티 전체를 가지고 있어도 되지만,
    // nickname 등만 쓰면, 굳이 userEntity 자체를 필드로 안 두는 방법도 있음
    // private User userEntity;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.attributes = attributes;
    }

    // OAuth2User 인터페이스
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        // 스프링 시큐리티가 식별자로 쓰는 값 (문자열)
        return String.valueOf(this.userId);
    }

    // MyPrincipal
    @Override
    public Long getUserId() {
        return this.userId;
    }

    @Override
    public String getNickname() {
        return this.nickname;
    }

    // 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
