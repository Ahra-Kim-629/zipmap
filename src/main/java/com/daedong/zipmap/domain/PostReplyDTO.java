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
    
    // 게시글 제목 (조인용)
    private String postTitle;
}
