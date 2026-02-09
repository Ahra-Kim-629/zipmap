package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewFile {
    private long id;
    private long review_id;
    private String file_path;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
