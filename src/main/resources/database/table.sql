------------------------------------------------------------
-- 기존 관련 테이블 모두 삭제 (새 구조용 테이블 포함)
-- (참고: DROP TABLE IF EXISTS 구문은 개발 중 스키마를 재설정할 때 사용)
------------------------------------------------------------

-- 자식 테이블부터 삭제 (참조하는 테이블이 삭제되기 전에 자식 테이블을 먼저 삭제해야 합니다)
DROP TABLE IF EXISTS Shared_Book_Replies;       -- Shared_Book_Replies는 Shared_Books와 Users를 참조
DROP TABLE IF EXISTS Shared_Book_Views;         -- Shared_Book_Views는 Shared_Books와 Users를 참조
DROP TABLE IF EXISTS Shared_Book_Likes;         -- Shared_Book_Likes는 Shared_Books와 Users를 참조
DROP TABLE IF EXISTS Used_Book_Kanjis;          -- Used_Book_Kanjis는 Book_Lines와 Kanjis를 참조
DROP TABLE IF EXISTS Community_Replies;         -- Community_Replies는 Community_Posts와 Users를 참조
DROP TABLE IF EXISTS Kanji_Collections;         -- Kanji_Collections는 Kanjis와 Users를 참조
DROP TABLE IF EXISTS Community_Posts;           -- Community_Posts는 Users를 참조
DROP TABLE IF EXISTS Book_Lines;                -- Book_Lines는 Books를 참조
DROP TABLE IF EXISTS Shared_Books;              -- Shared_Books는 Books를 참조
DROP TABLE IF EXISTS Books;                     -- Books는 Users를 참조
DROP TABLE IF EXISTS Social_Accounts;           -- Social_Accounts는 Users를 참조
DROP TABLE IF EXISTS Kanjis;                    -- Kanjis는 독립 테이블 (하지만 자식 테이블에서 참조됨)
DROP TABLE IF EXISTS Users;                     -- Users는 최상위 부모 테이블

------------------------------------------------------------
-- 테이블 생성
------------------------------------------------------------

------------------------------------------------------------
-- Users 테이블: 시스템의 사용자 정보를 저장합니다.
------------------------------------------------------------
CREATE TABLE Users (
    user_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,  -- 각 사용자를 고유하게 식별하는 기본키 (자동 증가)
    username VARCHAR(50) NOT NULL UNIQUE,                   -- 로그인에 사용되는 사용자 아이디 (중복 불가)
    password CHAR(60) NOT NULL,                             -- 암호화된 비밀번호 (고정 길이)
    nickname VARCHAR(50) NOT NULL UNIQUE,                   -- 사용자 표시 이름 (중복 불가)
    email VARCHAR(100) NOT NULL UNIQUE,                            -- 사용자 이메일 주소
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',          -- 사용자 권한 (기본값 'ROLE_USER')
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR')),
    status VARCHAR(20) NOT NULL                             -- 사용자 상태: ACTIVE, INACTIVE, BANNED, PENDING, DELETED 중 하나
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING', 'DELETED')),
    profile_url VARCHAR(255),                               -- 프로필 이미지 URL (선택적)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 계정 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 마지막 수정 일시
    PRIMARY KEY (user_id)
);

------------------------------------------------------------
-- Social_Accounts 테이블: 소셜 로그인 관련 정보를 저장합니다.
------------------------------------------------------------
CREATE TABLE Social_Accounts (
    social_account_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    user_id INT NOT NULL,
    provider VARCHAR(50) NOT NULL,            -- 소셜 플랫폼 이름 (예: 'google', 'github', 'discord')
    provider_user_id VARCHAR(100) NOT NULL,   -- 소셜 플랫폼에서의 사용자 식별자
    access_token VARCHAR(255),                -- (선택) 소셜 API 호출 시 필요한 액세스 토큰
    refresh_token VARCHAR(255),               -- (선택) 액세스 토큰 갱신용
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (social_account_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE (provider, provider_user_id)
);

------------------------------------------------------------
-- Kanjis 테이블: 한자 정보를 저장합니다.
------------------------------------------------------------
CREATE TABLE Kanjis (
    kanji_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 각 한자를 고유하게 식별하는 기본키
    jlpt_rank VARCHAR(10),                                  -- JLPT 등급 (예: N5, N4 등)
    category VARCHAR(100),                                   -- 한자의 분류 (예: 기초, 중급, 고급 등)
    kanji VARCHAR(4) NOT NULL,                              -- 실제 한자 문자 (최대 4자)
    kor_onyomi VARCHAR(100),                                 -- 한자의 한국식 온음
    kor_kunyomi VARCHAR(100),                                -- 한자의 한국식 훈독
    jpn_onyomi VARCHAR(100),                                 -- 한자의 일본식 온음
    jpn_kunyomi VARCHAR(100),                                -- 한자의 일본식 훈독
    meaning VARCHAR(2000),                                  -- 한자의 의미 또는 설명
    PRIMARY KEY (kanji_id)
);

------------------------------------------------------------
-- Books 테이블: 한 권의 책(스토리 전체)을 관리합니다.
------------------------------------------------------------
CREATE TABLE Books (
    book_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,   -- 책을 고유하게 식별하는 기본키
    user_id INT NOT NULL,                                    -- 책 작성자를 참조 (Users 테이블)
    title VARCHAR(100) NOT NULL,                             -- 책 제목
    thumbnail_url VARCHAR(255),                              -- 대표 이미지 URL (썸네일)
    is_ended BOOLEAN NOT NULL,                               -- 책 종료 여부 (true이면 종료)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 책 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 책 마지막 수정 일시
    PRIMARY KEY (book_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Book_Lines 테이블: 한 책을 구성하는 각 줄(또는 장) 정보를 저장합니다.
------------------------------------------------------------
CREATE TABLE Book_Lines (
    line_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,   -- 각 줄을 고유하게 식별하는 기본키
    book_id INT NOT NULL,                                    -- 이 줄이 속한 책을 참조 (Books 테이블)
    user_content TEXT,                                       -- 줄의 내용 (사용자)
    gpt_content TEXT,                                        -- 줄의 내용 (GPT)
    -- hp INT,                                                  -- 사용자의 현재 채력
    -- mp INT,                                                  -- 사용자의 현재 정신력
    -- current_location VARCHAR(100),                           -- 사용자의 현재 위치
    sequence  INT NOT NULL,                                -- 턴 수
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 줄 생성 일시
    PRIMARY KEY (line_id),
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Community_Posts 테이블: 커뮤니티 게시글 정보를 저장합니다.
------------------------------------------------------------
CREATE TABLE Community_Posts (
    community_post_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 게시글 고유 식별자
    user_id INT NOT NULL,                                            -- 게시글 작성자 (Users 테이블)
    title VARCHAR(100) NOT NULL,                                     -- 게시글 제목
    content TEXT NOT NULL,                                           -- 게시글 내용
    hit_count INT NOT NULL DEFAULT 0,                                -- 조회수
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,         -- 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,         -- 수정 일시
    PRIMARY KEY (community_post_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Shared_Books 테이블 : 한 권의 책을 공유할 때 사용하는 테이블
------------------------------------------------------------
-- Shared_Books 테이블은 사용자가 작성한 책(Books 테이블의 한 행)을 공유할 때 생성됩니다.
-- 이 테이블은 공유된 책의 조회수(hit_count)와 추천수(gaechu) 등의 통계 정보를 관리합니다.
------------------------------------------------------------
CREATE TABLE Shared_Books (
    shared_book_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 공유된 책의 고유 식별자
    book_id INT NOT NULL,                                          -- 공유되는 책을 참조하는 외래키 (Books 테이블)
    hit_count INT NOT NULL DEFAULT 0,                              -- 공유된 책의 조회수, 기본값 0
    gaechu INT NOT NULL DEFAULT 0,                                 -- 공유된 책의 추천 수 또는 좋아요 수, 기본값 0
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,        -- 공유 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,        -- 공유 정보 수정 일시
    PRIMARY KEY (shared_book_id),
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
    -- 책이 삭제되면, 해당 책의 공유 정보도 함께 삭제됩니다.
);


------------------------------------------------------------
-- Kanji_Collections 테이블
------------------------------------------------------------
-- Kanji_Collections 테이블은 사용자가 소장한 한자들을 모아둔 컬렉션입니다.
-- 각 컬렉션 항목은 해당 한자(kanji_id, Kanjis 테이블)와 컬렉션 소유자(user_id, Users 테이블)를 연결합니다.
------------------------------------------------------------
CREATE TABLE Kanji_Collections (
    kanji_collection_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 컬렉션 항목의 고유 식별자
    kanji_id INT NOT NULL,                                             -- 소장된 한자를 참조하는 외래키 (Kanjis 테이블)
    user_id INT NOT NULL,                                              -- 컬렉션 항목의 소유자, Users 테이블과 연결
    bookmarked TINYINT NOT NULL DEFAULT 0,                             -- 해당 한자를 즐겨찾기 했는지 여부
    collected_count INT NOT NULL DEFAULT 0,                            -- 해당 한자를 모은 횟수
    wrong_count INT NOT NULL DEFAULT 0,                                -- 해당 한자의 오답 횟수
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,           -- 컬렉션 항목 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,           -- 컬렉션 항목 수정 일시
    PRIMARY KEY (kanji_collection_id),
    FOREIGN KEY (kanji_id) REFERENCES Kanjis(kanji_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE  -- 사용자가 삭제되면, 해당 사용자의 한자 컬렉션 항목도 삭제됩니다.
);

------------------------------------------------------------
-- Community_Replies 테이블
------------------------------------------------------------
-- Community_Replies 테이블은 커뮤니티 게시글에 달린 댓글을 저장합니다.
-- 각 댓글은 어느 게시글(community_post_id, Community_Posts 테이블)과
-- 어느 사용자가 작성했는지(user_id, Users 테이블)를 연결합니다.
------------------------------------------------------------
CREATE TABLE Community_Replies (
    community_reply_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 댓글 고유 식별자
    community_post_id INT NOT NULL,                                  -- 댓글이 달린 게시글 (Community_Posts 테이블)
    user_id INT NOT NULL,                                            -- 댓글 작성자 (Users 테이블)
    content VARCHAR(2000) NOT NULL,                                  -- 댓글 내용
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 댓글 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 댓글 수정 일시
    PRIMARY KEY (community_reply_id),
    FOREIGN KEY (community_post_id) REFERENCES Community_Posts(community_post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Used_Book_Kanjis 테이블: 각 책 줄에서 사용된 한자 기록
------------------------------------------------------------
CREATE TABLE Used_Book_Kanjis (
    used_book_kanji_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 한자 사용 기록 고유 식별자
    line_id INT NOT NULL,                                             -- 사용된 줄 (Book_Lines 테이블)
    kanji_id INT NOT NULL,                                            -- 사용된 한자 (Kanjis 테이블)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 기록 생성 일시
    PRIMARY KEY (used_book_kanji_id),
    FOREIGN KEY (line_id) REFERENCES Book_Lines(line_id) ON DELETE CASCADE,
    FOREIGN KEY (kanji_id) REFERENCES Kanjis(kanji_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Shared_Book_Replies 테이블: 공유된 책에 대한 댓글
------------------------------------------------------------
CREATE TABLE Shared_Book_Replies (
    shared_book_reply_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 공유 책 댓글 고유 식별자
    shared_book_id INT NOT NULL,                                        -- 댓글이 달린 공유 책 (Shared_Books 테이블)
    user_id INT NOT NULL,                                               -- 댓글 작성자 (Users 테이블)
    content VARCHAR(2000) NOT NULL,                                     -- 댓글 내용
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,             -- 댓글 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,             -- 댓글 수정 일시
    PRIMARY KEY (shared_book_reply_id),
    FOREIGN KEY (shared_book_id) REFERENCES Shared_Books(shared_book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Shared_Book_Views 테이블: 공유 글 조회 기록 (중복 조회 방지)
------------------------------------------------------------
CREATE TABLE Shared_Book_Views (
    shared_book_view_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    shared_book_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (shared_book_view_id),
    UNIQUE (shared_book_id, user_id),
    FOREIGN KEY (shared_book_id) REFERENCES Shared_Books(shared_book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

------------------------------------------------------------
-- Shared_Book_Likes 테이블: 공유 글 추천(좋아요) 기록 (한 사용자당 한 번)
------------------------------------------------------------
CREATE TABLE Shared_Book_Likes (
    shared_book_like_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    shared_book_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (shared_book_like_id),
    UNIQUE (shared_book_id, user_id),
    FOREIGN KEY (shared_book_id) REFERENCES Shared_Books(shared_book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);
