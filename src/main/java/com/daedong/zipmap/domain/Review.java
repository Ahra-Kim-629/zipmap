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

    // 2/24 수정
    // private String reviewStatus;
    private Status reviewStatus;

    private long likeCount;
    private long viewCount;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
