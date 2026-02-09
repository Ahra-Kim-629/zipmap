package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Cons {
    private long id;
    private long review_id;
    private String attribute;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
