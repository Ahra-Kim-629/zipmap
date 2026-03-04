package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private long id;
    private String title;
    private String content;
    private long userId;
    private Category category;
    private Location location;
    private long likeCount;
    private long viewCount;

    // 2/24 수정
    // private String postStatus;
    private Status postStatus;

    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
