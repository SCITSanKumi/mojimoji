package com.sangkeumi.mojimoji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(name = "Shared_Book_Likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "shared_book_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBookLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedBookLikeId;

    @ManyToOne
    @JoinColumn(name = "shared_book_id", nullable = false)
    private SharedBook sharedBook;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
