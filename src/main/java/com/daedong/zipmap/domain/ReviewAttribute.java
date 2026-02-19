package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewAttribute {
    private long id;
    private long reviewId;
    private String type;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
