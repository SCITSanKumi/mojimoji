-- 기존 관련 테이블 모두 삭제 (새 구조용 테이블 포함)
-- (참고: DROP TABLE IF EXISTS 구문은 개발 중 스키마를 재설정할 때 사용)


DROP TABLE IF EXISTS Shared_Book_Replies;
DROP TABLE IF EXISTS Shared_Book_Views;
DROP TABLE IF EXISTS Shared_Book_Likes;
DROP TABLE IF EXISTS Shared_Books;
DROP TABLE IF EXISTS Used_Book_Kanjis;
DROP TABLE IF EXISTS Book_Lines;
DROP TABLE IF EXISTS Books;
DROP TABLE IF EXISTS Community_Replies;
DROP TABLE IF EXISTS Kanji_Collections;
DROP TABLE IF EXISTS Community_Posts;
DROP TABLE IF EXISTS Kanjis;
DROP TABLE IF EXISTS Social_Accounts;
DROP TABLE IF EXISTS Users;

------------------------------------------------------------
-- Users 테이블
------------------------------------------------------------
-- Users 테이블은 시스템의 사용자 정보를 저장합니다.
-- 각 사용자에게는 고유한 user_id가 부여되며, 로그인 정보(username, password)
--와 프로필 관련 정보(nickname, email, profile_url) 등을 저장합니다.
-- 또한, 사용자의 역할(role)과 상태(status)를 관리하며, 생성일(created_at)과 수정일(updated_at) 정보도 포함됩니다.
------------------------------------------------------------
CREATE TABLE Users (
    user_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,  -- 각 사용자를 고유하게 식별하는 기본키 (자동 증가)
    username VARCHAR(50) NOT NULL UNIQUE,                   -- 로그인에 사용되는 사용자 아이디, 중복 불가
    password CHAR(60) NOT NULL,                             -- 암호화된 비밀번호 (고정 길이)
    nickname VARCHAR(50) NOT NULL UNIQUE,                   -- 사용자 표시 이름, 중복 불가
    email VARCHAR(100) NOT NULL,                            -- 사용자 이메일 주소
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',          -- 사용자 권한, 기본값은 'ROLE_USER'
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR')),
    status VARCHAR(20) NOT NULL                             -- 사용자 상태: active, inactive, banned, pending, deleted 중 하나
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING', 'DELETED')),
    profile_url VARCHAR(255),                               -- 프로필 이미지 URL (선택적)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 계정 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 마지막 수정 일시
    PRIMARY KEY (user_id)
);

CREATE TABLE Social_Accounts (
    social_account_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    user_id INT NOT NULL,
    provider VARCHAR(50) NOT NULL,            -- 예: 'google', 'kakao', 'facebook'
    provider_user_id VARCHAR(100) NOT NULL,   -- 해당 소셜 플랫폼에서 유저 식별자 (예: Google의 sub 필드)
    access_token VARCHAR(255),                -- (선택) 소셜 API 호출 시 필요한 액세스 토큰
    refresh_token VARCHAR(255),               -- (선택) 액세스 토큰 갱신용
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (social_account_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE (provider, provider_user_id)       -- 동일 소셜+식별자 중복 가입 방지
);


------------------------------------------------------------
-- Kanjis 테이블
------------------------------------------------------------
-- Kanjis 테이블은 한자 정보를 저장합니다.
-- 각 한자는 고유한 kanji_id로 식별되며, 일본어능력시험(JLPT) 등급, 분류(category),
-- 실제 한자 문자(kanji), 발음(온음, 훈독) 및 의미(meaning) 등의 정보를 포함합니다.
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
-- Books 테이블 : 한 권의 책(스토리 전체)을 관리
------------------------------------------------------------
-- Books 테이블은 한 사용자가 작성한 전체 스토리(한 권의 책)에 대한 기본 정보를 저장합니다.
-- 여기에는 책 제목, 대표 이미지(썸네일) 및 책 작성자(user_id)가 포함됩니다.
-- 한 권의 책은 여러 줄(Book_Lines)로 구성되며, 사용자가 작성한 책 단위로 조회하거나 공유할 수 있습니다.
------------------------------------------------------------
CREATE TABLE Books (
    book_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,   -- 책을 고유하게 식별하는 기본키
    user_id INT NOT NULL,                                    -- 이 책의 작성자를 참조하는 외래키 (Users 테이블)
    title VARCHAR(100) NOT NULL,                             -- 책 제목
    thumbnail_url VARCHAR(255),                              -- 책의 대표 이미지 URL (썸네일)
    is_ended BOOLEAN NOT NULL,                               -- 책의 종료 여부 (true이면 책 종료)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 책 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 책 마지막 수정 일시
    PRIMARY KEY (book_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
    -- 사용자가 삭제되면, 해당 사용자가 작성한 책도 함께 삭제됩니다.
);


------------------------------------------------------------
-- Book_Lines 테이블 : 한 책을 구성하는 각 줄(또는 장) 정보를 저장
------------------------------------------------------------
-- Book_Lines 테이블은 하나의 책(Books 테이블에 기록된)의 내용을 여러 줄로 나누어 저장합니다.
-- 각 줄은 책 내에서의 순서(sequence)를 가지고 있으며, 줄의 내용(content),
-- 해당 줄이 플레이된 횟수(played_turns)와 책의 끝인지 여부(is_ended)를 기록합니다.
------------------------------------------------------------
CREATE TABLE Book_Lines (
    line_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,   -- 각 줄을 고유하게 식별하는 기본키
    book_id INT NOT NULL,                                    -- 이 줄이 속하는 책을 참조하는 외래키 (Books 테이블)
    role VARCHAR(10) NOT NULL,                               -- 사용자의 대답인지 openAI의 대답인지 ("user", "assistant", 또는 "system")
    content TEXT,                                            -- 해당 줄의 텍스트 내용 (스토리의 한 부분)
    hp INT NOT NULL,
    mp INT NOT NULL,
    sequence INT NOT NULL,                                   -- 책 내에서의 순서를 나타냅니다. (예: 1, 2, 3, ...)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 줄 생성 일시
    PRIMARY KEY (line_id),
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
    -- 책이 삭제되면, 해당 책의 모든 줄도 함께 삭제됩니다.
);

------------------------------------------------------------
-- Community_Posts 테이블
------------------------------------------------------------
-- Community_Posts 테이블은 사용자가 작성한 커뮤니티 게시글 정보를 저장합니다.
-- 각 게시글은 제목(title), 내용(content), 조회수(hit_count) 등을 포함하며,
-- 작성자(user_id)를 통해 Users 테이블과 연결됩니다.
------------------------------------------------------------
CREATE TABLE Community_Posts (
    community_post_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 게시글 고유 식별자
    user_id INT NOT NULL,                                            -- 게시글 작성자를 참조하는 외래키 (Users 테이블)
    title VARCHAR(100) NOT NULL,                                     -- 게시글 제목
    content TEXT NOT NULL,                                           -- 게시글 내용 (자세한 텍스트)
    hit_count INT NOT NULL DEFAULT 0,                                -- 게시글의 조회수, 기본값 0
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,         -- 게시글 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,         -- 게시글 수정 일시
    PRIMARY KEY (community_post_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
    -- 사용자가 삭제되면, 해당 사용자의 게시글도 함께 삭제됩니다.
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,            -- 컬렉션 항목 생성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,            -- 컬렉션 항목 수정 일시
    PRIMARY KEY (kanji_collection_id),
    FOREIGN KEY (kanji_id) REFERENCES Kanjis(kanji_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
    -- 사용자가 삭제되면, 해당 사용자의 한자 컬렉션 항목도 삭제됩니다.
);


------------------------------------------------------------
-- Community_Replies 테이블
------------------------------------------------------------
-- Community_Replies 테이블은 커뮤니티 게시글에 달린 댓글을 저장합니다.
-- 각 댓글은 어느 게시글(community_post_id, Community_Posts 테이블)과
-- 어느 사용자가 작성했는지(user_id, Users 테이블)를 연결합니다.
------------------------------------------------------------
CREATE TABLE Community_Replies (
    community_reply_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 댓글의 고유 식별자
    community_post_id INT NOT NULL,                                  -- 댓글이 달린 게시글을 참조 (Community_Posts 테이블)
    user_id INT NOT NULL,                                            -- 댓글 작성자를 참조 (Users 테이블)
    content VARCHAR(2000) NOT NULL,                                  -- 댓글 내용 (최대 2000자)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 댓글 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 댓글 수정 일시
    PRIMARY KEY (community_reply_id),
    FOREIGN KEY (community_post_id) REFERENCES Community_Posts(community_post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
    -- 게시글이나 사용자가 삭제되면 해당 댓글도 삭제됩니다.
);


------------------------------------------------------------
-- Used_Book_Kanjis 테이블 : 각 책의 줄(Book_Lines)에서 사용된 한자를 기록
------------------------------------------------------------
-- Used_Book_Kanjis 테이블은 책의 각 줄에서 어떤 한자가 사용되었는지에 대한 기록입니다.
-- 이를 통해 특정 줄에서 사용된 한자 정보를 추적하거나, 통계를 낼 수 있습니다.
-- 또한, 해당 한자가 힌트로 사용되었는지를 is_hint 필드로 구분합니다.
------------------------------------------------------------
CREATE TABLE Used_Book_Kanjis (
    used_book_kanji_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 한자 사용 기록의 고유 식별자
    line_id INT NOT NULL,                                             -- 이 기록이 속한 책의 줄을 참조 (Book_Lines 테이블)
    kanji_id INT NOT NULL,                                            -- 사용된 한자를 참조 (Kanjis 테이블)
    -- is_hint BOOLEAN NOT NULL,                                         -- 해당 한자가 힌트로 사용되었는지 여부 (true/false)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,          -- 한자 사용 기록 생성 일시
    PRIMARY KEY (used_book_kanji_id),
    FOREIGN KEY (line_id) REFERENCES Book_Lines(line_id) ON DELETE CASCADE,
    FOREIGN KEY (kanji_id) REFERENCES Kanjis(kanji_id) ON DELETE CASCADE
    -- 책의 줄이나 한자가 삭제되면, 해당 한자 사용 기록도 삭제됩니다.
);


------------------------------------------------------------
-- Shared_Book_Replies 테이블 : 공유된 책에 대한 댓글
------------------------------------------------------------
-- Shared_Book_Replies 테이블은 사용자가 공유된 책(Shared_Books 테이블)에 대해 남긴 댓글을 저장합니다.
-- 각 댓글은 어느 공유된 책에 달렸는지(shared_book_id)와 댓글 작성자(user_id)를 연결합니다.
------------------------------------------------------------
CREATE TABLE Shared_Book_Replies (
    shared_book_reply_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY, -- 공유된 책 댓글의 고유 식별자
    shared_book_id INT NOT NULL,                                        -- 댓글이 달린 공유된 책을 참조 (Shared_Books 테이블)
    user_id INT NOT NULL,                                               -- 댓글 작성자를 참조 (Users 테이블)
    content VARCHAR(2000) NOT NULL,                                     -- 댓글 내용
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,             -- 댓글 작성 일시
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,             -- 댓글 수정 일시
    PRIMARY KEY (shared_book_reply_id),
    FOREIGN KEY (shared_book_id) REFERENCES Shared_Books(shared_book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
    -- 공유된 책이나 사용자가 삭제되면, 해당 댓글도 함께 삭제됩니다.
);


------------------------------------------------------------
-- Shared_Book_Views 테이블
------------------------------------------------------------
-- 이 테이블은 한 사용자가 특정 공유 글을 조회했는지 기록합니다.
-- 동일 사용자가 같은 글을 여러 번 조회해도 한 번만 조회 수가 증가하도록 합니다.
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
-- Shared_Book_Likes 테이블
------------------------------------------------------------
-- 이 테이블은 한 사용자가 특정 공유 글에 추천(좋아요)를 눌렀는지 기록합니다.
-- UNIQUE 제약조건을 통해 한 사용자는 한 글에 대해 한 번만 추천할 수 있도록 합니다.
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
