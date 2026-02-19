package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyDTO {
    private long id;
    private String targetType;
    private long targetId;
    private long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;
}
