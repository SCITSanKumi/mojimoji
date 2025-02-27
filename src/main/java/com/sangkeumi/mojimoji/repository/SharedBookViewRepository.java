package com.sangkeumi.mojimoji.repository;

import com.sangkeumi.mojimoji.entity.SharedBook;
import com.sangkeumi.mojimoji.entity.SharedBookView;
import com.sangkeumi.mojimoji.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedBookViewRepository extends JpaRepository<SharedBookView, Long> {
    boolean existsBySharedBookAndUser(SharedBook sharedBook, User user);
}
