package com.sangkeumi.mojimoji.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    /**
     * Primary Key, 자동 생성
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 사용자 로그인 아이디, 유니크
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 암호화된 패스워드
     */
    @Column(nullable = false, length = 60)
    private String password;

    /**
     * 닉네임, 유니크
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    /**
     * 이메일 주소
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 사용자 권한, 기본값 "ROLE_USER"
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "ROLE_USER";

    /**
     * 사용자 상태 (ACTIVE, INACTIVE, BANNED, PENDING, DELETED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.INACTIVE;

    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_url")
    private String profileUrl;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 최종 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 양방향 관계 설정 (유저가 작성한 책, 커뮤니티 게시글, 댓글, 한자 컬렉션, 공유된 책 댓글 등)

    /**
     * 유저가 작성한 책(스토리) 목록 (Books 테이블과 연관)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books;

    /**
     * 유저가 작성한 커뮤니티 게시글 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityPost> communityPosts;

    /**
     * 유저가 작성한 커뮤니티 댓글 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityReply> communityReplies;

    /**
     * 유저의 한자 컬렉션 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanjiCollection> kanjiCollections;

    /**
     * 유저가 작성한 공유된 책 댓글 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedBookReply> sharedBookReplies;

    /**
     * 유저의 소셜 로그인 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts;

    // ★ KanjiCollectionSummary와 1:N 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanjiCollectionSummary> kanjiCollectionSummaries;

    public enum UserStatus {
        ACTIVE, INACTIVE, BANNED, PENDING, DELETED
    }
}
