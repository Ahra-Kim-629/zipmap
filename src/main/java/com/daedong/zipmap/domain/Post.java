package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String category;
    private String location;
    private int likeCount;
    private int dislikeCount;
    private int viewCount;
    private String postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
