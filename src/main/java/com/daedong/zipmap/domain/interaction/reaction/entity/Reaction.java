package com.daedong.zipmap.domain.interaction.reaction.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reaction {
    private long id;
    private String targetType; // 'post' or 'review'
    private long targetId;
    private Long userId;
    private Integer type; // 1 for like, -1 for dislike
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
