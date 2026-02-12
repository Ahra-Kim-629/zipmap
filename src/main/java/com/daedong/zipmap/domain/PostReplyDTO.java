package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostReplyDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String loginId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
