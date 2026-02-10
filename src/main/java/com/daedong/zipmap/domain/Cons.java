package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Cons {
    private long id;
    private long reviewId;
    private String attribute;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
