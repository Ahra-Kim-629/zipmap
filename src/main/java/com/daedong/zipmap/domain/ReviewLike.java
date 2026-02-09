package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewLike {
    private long id;
    private long user_id;
    private long review_id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
