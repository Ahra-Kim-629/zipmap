package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private long id;
    private String title;
    private String content;
    private long userId;
    private String category;
    private String location;
    private long likeCount;
    private long viewCount;
    private String postStatus;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
