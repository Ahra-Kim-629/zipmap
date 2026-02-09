package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewReply {
    private long id;
    private long review_id;
    private long user_id;
    private String content;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
