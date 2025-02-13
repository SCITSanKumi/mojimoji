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
    @Column(name = "shared_story_id")
    private Long sharedStoryId;

    @OneToOne
    @JoinColumn(name = "story_id", nullable = false, unique = true)
    private Story story;

    @Column(name = "hit_count", nullable = false)
    private int hitCount;

    @Column(name = "gaechu", nullable = false)
    private int gaechu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sharedStory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedStoryReply> sharedStoryReplies;
}