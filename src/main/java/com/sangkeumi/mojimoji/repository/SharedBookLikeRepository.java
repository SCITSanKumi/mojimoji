package com.sangkeumi.mojimoji.repository;

import java.util.Optional;
import com.sangkeumi.mojimoji.entity.SharedBook;
import com.sangkeumi.mojimoji.entity.SharedBookLike;
import com.sangkeumi.mojimoji.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedBookLikeRepository extends JpaRepository<SharedBookLike, Long> {
    Optional<SharedBookLike> findBySharedBookAndUser(SharedBook sharedBook, User user);
}
