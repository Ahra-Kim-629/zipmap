package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewFile {
    private long id;
    private long reviewId;
    private String filePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
