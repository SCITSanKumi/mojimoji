package com.sangkeumi.mojimoji.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sangkeumi.mojimoji.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
