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

    /**
     * 소셜에서 가져온 사용자 정보(예: {sub=..., email=..., name=...}).
     * - sub: 구글 OIDC에서 제공하는 사용자 고유 ID
     * - email, name, picture 등 다양한 필드가 포함될 수 있음
     */
    private Map<String, Object> attributes;

    /**
     * 프로젝트 내의 User 엔티티 객체.
     * - 소셜 로그인 후 DB에 저장되거나, 이미 존재하는 User 엔티티와 매핑됨.
     * - 이후 인증 로직에서 도메인 정보(닉네임, 권한 등)를 활용할 수 있음.
     */
    private User userEntity;

    /**
     * 생성자
     *
     * @param attributes 소셜에서 내려준 사용자 정보 맵
     * @param userEntity DB의 User 엔티티 객체
     */
    public CustomOAuth2User(Map<String, Object> attributes, User userEntity) {
        this.attributes = attributes;
        this.userEntity = userEntity;
    }

    /**
     * OAuth2User 인터페이스 구현 메서드
     * 
     * 소셜에서 내려준 사용자 정보(Claim/Attributes)를 반환.
     * 
     * @return 소셜 사용자 정보 (ex: {sub=..., email=..., name=...})
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * OAuth2User 인터페이스 구현 메서드
     * 
     * 스프링 시큐리티가 사용자의 "이름"을 식별하기 위해 사용하는 메서드.
     * 보통 구글 OIDC에서는 "sub" 필드를 반환하지만,
     * 여기서는 DB에 저장된 userEntity가 있다면 userId를 문자열로 변환하여 반환.
     * 
     * @return 사용자 식별자 (기본적으로 DB의 user_id.toString() 또는 sub)
     */
    @Override
    public String getName() {
        // userEntity가 null이 아니라면 DB의 user_id를 사용,
        // 아니라면 attributes에서 sub 필드를 사용.
        return userEntity != null
                ? userEntity.getUserId().toString()
                : (String) attributes.get("sub");
    }

    @Override
    public Long getUserId() {
        return userEntity.getUserId();
    }

    @Override
    public String getNickname() {
        return userEntity.getNickname();
    }

    /**
     * OAuth2User 인터페이스 구현 메서드
     * 
     * 사용자가 가진 권한(Authority) 목록을 반환.
     * 여기서는 예시로 "ROLE_USER" 하나만 부여하고 있음.
     * 
     * @return GrantedAuthority의 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 단일 권한 "ROLE_USER"만 주는 예시
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * 커스텀 메서드
     * 
     * 실제 프로젝트의 User 엔티티 객체를 반환.
     * - 로그인 후 컨트롤러/서비스 계층에서
     * userEntity 정보를 활용할 때 편리.
     * 
     * @return DB의 User 엔티티
     */
    public User getUserEntity() {
        return userEntity;
    }
}
