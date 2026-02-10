package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewLike {
    private long id;
    private long userId;
    private long reviewId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
