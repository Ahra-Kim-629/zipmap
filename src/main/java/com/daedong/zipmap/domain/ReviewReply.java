package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewReply {
    private long id;
    private long reviewId;
    private long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;
}
