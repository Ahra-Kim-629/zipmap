package com.daedong.zipmap.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private long id;
    private String title;
    private String content;
    private String address;
    private long userId;
    private int point;
    private String reviewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;

    // ✅ 장점 / 단점 필드 추가
    private String pros;
    private String cons;
}
