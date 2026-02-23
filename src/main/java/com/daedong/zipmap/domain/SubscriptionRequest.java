package com.daedong.zipmap.domain;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRequest {
    private Long userId;
    private List<String> keywords;
    private String targetType;
}
