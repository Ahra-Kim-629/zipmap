package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostReaction {
    private Long id;
    private Long postId;
    private Long userId;
    private int type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}