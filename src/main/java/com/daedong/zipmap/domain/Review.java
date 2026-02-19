package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private long id;
    private String title;
    private String content;
    private String address;
    private long userId;
    private int point;
    private String reviewStatus;
    private long likeCount;
    private long viewCount;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
