package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmDTO {
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private String message;
    private String isRead;
    private LocalDateTime createdAt;

    public String getMoveUrl() {
        if ("POST".equals(targetType)) {
            return "/post/detail?id=" + targetId;
        } else {
            return "/review/detail?id=" + targetId;
        }
    }
}
