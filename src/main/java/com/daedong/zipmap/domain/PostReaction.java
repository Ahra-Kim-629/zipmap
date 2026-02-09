package com.daedong.zipmap.domain;

import lombok.Data;

@Data
public class PostReaction {
    private Long id;
    private Long postId;
    private Long userId;
    private int type;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
