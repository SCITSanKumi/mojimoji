package com.sangkeumi.mojimoji.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 계정을 저장하는 엔티티
 * Users 테이블과 1:N 관계 (한 명의 유저가 여러 소셜 계정을 연결 가능)
 */
@Entity
@Table(name = "Social_Accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    /**
     * 기본키, 자동 증가
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long socialAccountId;

    /**
     * Users 테이블과 N:1 관계 (user_id 컬럼이 FK)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 소셜 플랫폼 제공자 (예: 'google', 'kakao', 'facebook' 등)
     */
    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    /**
     * 소셜 플랫폼에서 제공하는 유저 식별자 (예: 구글 OAuth의 sub)
     */
    @Column(name = "provider_user_id", length = 100, nullable = false)
    private String providerUserId;

    /**
     * (선택) 소셜 API 호출에 필요한 Access Token
     */
    @Column(name = "access_token", length = 255)
    private String accessToken;

    /**
     * (선택) Access Token 갱신용 Refresh Token
     */
    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    /**
     * 레코드 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 레코드 수정 시각
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

}
