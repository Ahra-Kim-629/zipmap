package com.daedong.zipmap.domain;


import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private long id;
    private String title;
    private String content;
    private String address;
    private long user_id;
    private int point;
    private String review_status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private LocalDateTime updated_at;

    private String loginId; // login_id -> loginId 로 변경 (CamelCase 매핑 대응)

}
