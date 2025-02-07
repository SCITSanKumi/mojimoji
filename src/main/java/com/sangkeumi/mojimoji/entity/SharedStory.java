package com.sangkeumi.mojimoji.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Shared_Stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedStoryId;

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(nullable = false)
    @Builder.Default
    private int hitCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int gaechu = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sharedStory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedStoryReply> sharedStoryReplies;
}
