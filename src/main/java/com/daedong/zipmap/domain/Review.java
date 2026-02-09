package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private long id;
    private String title;
    private String content;
    private String address;
    private long user_id;
    private int point;
    private String review_status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
