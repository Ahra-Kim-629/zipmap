package com.daedong.zipmap.domain.subscription.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long id;
    private Long userId;
    private String keyword;
    private String targetType;
    private LocalDateTime createdAt;
}
